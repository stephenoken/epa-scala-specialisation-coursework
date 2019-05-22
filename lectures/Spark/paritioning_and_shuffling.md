# Partitioning and Shuffling
---
## Shuffling: What it is and why it's important

![shuffling](./resources/shuffling-group-by-key.png)

We typically have to move data from one node to another to be "grouped with" it's key. Doing this is called "shuffling".

**Shuffles Happen**

Shuffles can be an enormous hit to performance because it means that Spark must send data from one node to another. Why? **Latency!!**

Let's start with an example. Given:

```
case class CFFPurchase(customerId: Int, destination: String, price: Double)
```

Assume we have an RDD of the purchases that users of the Swiss train compnay's the CFF's, mobile app have made in the past month.

```
val purchasesRdd: RDD[CFFPurchase] = sc.textFile(..)
```

**Goal: calculate how many trips, and how much money was spent by each customer over the course of the month?**

```
val purchasesPerMonth =
  purchasesRdd.map(p => (p.customerId, p.price)) // Pair RDD
              .groupByKey() // groupByKey returns RDD[K, Iterable[V]]
              .map(p => (p._1, (p._2.size, p._2.sum)))
              .collect()

val purchases = List(CFFPurchase(100, "Geneva", 22.25),
                    CFFPurchase(300, "Zurich", 42.10),
                    CFFPurchase(100, "Fribourg", 42.10),
                    CFFPurchase(200, "Zurich", 42.10),
                    CFFPurchase(100, "Zurich", 42.10),
                    CFFPurchase(300, "Zurich", 42.10),
                    )
```
What might the cluster look like with this data distributed over it?

Starting with purchasesRDD:
![what's happening with a shuffling](./resources/shuffling-what-happens.png)

![shuffling Latency](./resources/shuffling-latency-memory.png)

**We don't want to be sending all of our data over the network if it's not absolutely required. Too much network communication kills performance.**

Is there a way to not send all the pairs over the network?

Perhaps we can reduce before we shuffle. This could greatly reduce the amount of data we have to send over the network.

### Grouping and Reducing

We can use reduceByKey.

Conceptually, reduceByKey can be thought of as a combination of first groupByKey and then reducing on all the values grouped per key. It's more efficient though, than using each seprately. We'll see how in the following example.

**Signature**
```
def reduceByKey(func: (V, V) => V): RDD[(K, V)]
```

```
val purchasesPerMonth =
  purchasesRdd.map(p => (p.customerId, (1, p.price))) // Pair RDD
  .reduceByKey((acc, curr) => (acc._1 + curr._1, acc._2 + curr._2))
  .collect()
```
![shuffling performance optimisation](./resources/shuffling-optimised-reduction.png)


**What are the benifits of this approach?**

By reducing the dataset first, the amount of data sent over the network during the shuffling is greatly reduced.

This can result in non-trivial gains in performance!

 Grouping all the key-value pairs with the same key requires collecting all key-value pairs with the same key on the same machine.

 __But how does Spark know which key to put on which machine?__

* By default, Spark uses hash partitioning to determine which key-value pair should be sent to which machine.
---
## Partitioning

Proper partitioning can save us a lot of time in our computations.

In the last section, we were looking at an example involving groupByKey, before we discovered that this operation causes data to be shuffled over the network.

> Grouping all values of key-value pairs with the same key requires collecting all key-values pairs with the same key on the same machine.

We concluded in the last session asking ourselves,
**But how does Spark know which key to put on which machine?**

**Before we try to optimise that example any further, let's first take a quick detour into what partitioning is...**

### Partitions

The data within RDD is split into several partitions:

**Properties of partitions**

* Partitions never span multiple machines, i.e, tuples in the same partition are guaranteed to be on the same machine.
* Each machine in the cluster contains one or more partitions.
* The number of partitions to use is configurable. By default, it equals the total number of cores on all executor nodes.

**Two kinds of partitioning available in Spark:**

* Hash partitioning
* Range partitioning

*Customizing a partitioning is only possible on Pair RDDS.*

### Hash Partitioning

Back to our example. Given a Pair RDD that should be grouped:

```
val purchasePerCust =
  purchasesRDD.map(p => (p.customerId, p.price))
  .groupByKey()
```

groupByKey first computes per tuple (k, v) its partition p:
```
p = k.hashCode() % numPartitions
```
Then all tuples in the same partition p are sent to the machine hosting p.

**Intuition: hash partitioning attempts to spread data evenly across partitions based on the key.**


### Range Partitioning

Pair RDDs may contain keys that have an ordering defined
* Examples: Int, Char, String, ...

For such RDDs, range partitioning may be more efficient.
Using a range partitioner, keys are partitioned according to:
1. an ordering for keys
2. a set of sorted ranges of keys
Property: tuples with keys in the same range appear on the same machine.

### Hash Partitioning: Example

Consider a Pair RDD, with keys [8, 96, 240, 400, 401, 800], and a desired number of partitions of 4.

Furthermore, supposes that hashCode() is the identity (n.hashCode() == n).

In this case, hash partitioning distributes the keys as follows among the partitions:
* partition 0: [8, 96, 240, 400, 800]
* partition 1: [401]
* partition 2: []
* partition 3: []

The result is a very unbalanced distribution which hurts performance.

### Range partitioning: Example
Using range partitioning the distribution can be imporved significantly:
* Assumptions: (a) keys non-negative, (b) 800 is biggest key in the RDD.
*  Set of ranges: [1, 200], [201, 400], [401, 600], [601, 800]

In this case, range partitioing distributes the keys as follows among the partitions:
* partition 0: [8, 96]
* partition 1: [240, 400]
* partition 2: [401]
* partition 3: [800]

The resulting partitioning is much more balanced.

**How do we set a partitioning for our data?**
There are two ways to create RDDs with specific partitionings:
1. Call partitionBy on an RDD, providing an explicit Partitioner.
2. Using transformations that return RDDs with specific partitioners.

### partitionBy

Invoking partitionBy creates an RDD with a specific partitioner.

Example:

```
val paris = purchasesRDD.map(p => (p.customerId, p.price))

val tunedPartitioner = new RangePartitioner(8, pairs)
val partitioned = pairs.partitionBy(tunedPartitioner).persist()

```

Create a RangePartitioner requires:
1. Specifying the desired number of partitions.
2. Providing a Pair RDD with ordered keys. This RDD is sampled to create a suitable set of sorted ranges.

**Important: the results of partitionBy should be persisted. Otherwise, the partitioning is repeatedly applied (involving shuffling!) each time the partitioned RDD is used.**

### Partitioning Data Using Transformations

**Partitioner form parent RDD:**

Pair RDDs that are the result of a transformation on a partitioned Pair RDD typically is configured to use the hash partitioner that was used to construct it.

**Automatically-set partitioners:**

Some operations on RDDs automatically result in an RDD with a known partitioner - for when it makes sense.

For example, by default, when using sortByKeys, a RangePartitioner is used. Further, the default partitioner when using groupByKey, is a HashPartitioner, as we saw earlier.

### Partitioning Data Using Transformations
Operations on Pair RDDs that hold to (and propagate) a partitioner.

![partitioning data via transformations](./resources/partitioning-data-using-transformations.png)

**All other operations will produce a result with a partitioner**

*Note the absence of map and flatMap*

**Why?**

Consider the map transformations. Given that we have a hash partitioned Pair RDD, why it make sense for map to lose the partitioner in its result RDD?
Because it's possible for map to change the key. EG..
```
rdd.map((k: String, v: Int) -> ("doh!", v))
```
In this case, if the map transformation preserved the partitioner in the result RDD, it no longer make sense, as now the keys are all different.

**Hence mapValues. It enables us to still do map transformations without changing the keys, thereby preserving the partitioner**

---

## Optimising with Partitions

We saw in the last session that Spark makes  a few kinds of partitioners available out of the box to users:

* hash partitioners
* range partitioners

We also learned what kinds of operations may introduce new partitioners, or which may discard custom partitioners

However, we haven't covered why someone would want to repartition their data.

**Partitioning can bring substantial performance gains, especially in the face of shuffles.**

Using range partitioners we can optimise our earlier use of reduceByKey so that it does not involve any shuffling over the network at all!

```
val pairs = purchasesRdd.map(p => (p.customerId, p.price))
val tunedPartitioner = new RangePartitioner(8, pairs) // 8 might make sense for the cluster

val partitioned = pairs.partitionBy(tunedPartitioner).persist()

val purchasePerCust = partitioned.map(p => (p._1, (1, p._2)))

val purchasesPerMonth = purchasePerCust
  .reduceByKey((v1, v2) => (v1._1 + v2._1, v1._2 + v2._2))
  .collect()
```

![optimisation result 1 ](./resources/partition-optimisation-results-1.png)

### Partitioning Data: partitionBy Example

Consider an application that keeps a large table of user information in memory:
* userData - BIG, containing (UserId, UserInfo) pairs, where UserInfo contains a list of topics the user is subscribed to.

The application periodically combines this big table with a smaller file representing events that happened in the past 5 minutes.

* events - small, containing (UserId, LinkInfo) pairs for users who have clocked a link on a website in those 5 minutes:

For example, we may wish to count how many users visited a link that was not to one of their subscribed topics. We can perfrom this combination with Spark's join operation, which can be used to group the UserInfo and LinkedInfo pairs for each UserUd by Key.


```
val sc = new SparkContext(..)
val userDate = sc.sequenceFile[UserId, UserInfor]("hdfs://...").persist()

def processNewLogs(logFileName: String) {
  val events = sc.sequenceFile[UserId, LinkInfo](logFileName)
  val joined = userDate.join(events) // RDD of (UserId, (UserInfo, LinkInfo))
  val offTopicVisits = joined.filter {
    cases (userId, (userInfo, linkInfo)) => // Expand the tuple
      !userInfo.topics.contains(linkInfo.topic)
  }.count()
  println("Number of visits to non-subscribed topics " + offTopicVisits)
}
```

**Is this OK?**

**It will be very inefficient!!**
**Why?** The join operation, called each time processNewLogs is invoked, does not know anything about how the keys are partitioned in the datasets.

By default, this operation will hash all the keys of both datasets, sending elements with the same key hash across the network to the same machine with the same key on that machine. **Even though userData doesn't change!!** ![partition data example](./resources/partition-data-partitionBy-example-1.png)

Fixingthis is easy. Just use partitionBy on the big userData RDD at the start of the program!

Therefore, userDate becomes:
```
val userData = sc.seqeunceFile[UserId, UserInfo]("hdfs://...")
  .partitionBy(new HashPartitioner(100)) // create 100 partitions
  .persist()
```

Since we called partitionBy when building userData, Spark will know that it is hash-partitioned, and calls to join on it will take advantage of this information.

In particular, when we call userData.join(events), Spark will shuffle only the events RDD, sending events wit heach particular UserID to the machine that contains the corresponding hash partition of userData.
![partition data example](./resources/partition-data-partitionBy-example-2.png)
Now that userData is pre-partitioned, Sprk will shuffle only the events RDD, sending events with each particular UserId to the machine that contains the corresponding hash partition of UserData.

### Back to Shuffling

Recall our example using groupByKey:
```
val purchasePerCust =
  purchasesRDD.map(p => (p.customerId, p.price))
  .groupByKey()
```
Grouping all values of key-value pairs with the same key requires collecting all key-value pairs with the same key on the same machine.

Grouping is done using a hash partitioner with default parameters.

The result RDD, purchasePerCust, is configured to use the hash partitioner that was used to construct it.

**Rule of thumb:** a shuffle can occur when the resulting RDD depends on other elements from the same RDD or another RDD.

You can also figure out whether a shuffle has been planned/ executed via:

1. The return type of certains transformations, e.g,
`org.apache.spark.rdd.RDD[(String, Int)] = ShuffledRDD[366]`

2. Using function toDebugString to see its execution plan:
```
partitioned.reduceByKey((v1, v2) => ...).toDebugString()

res9: String =
(8) MapPartitionsRDD[622] at reduceByKey at <console>:49 []
 |  ShuffledRDD[615] at partitionBy at <console>: 48
 |    CachedPartitions: 8; MemorySize: 1754.9 MB; DiskSize: 0.0 B
 ```

### Operations that might cause a shuffle

* cogroup
* groupWith
* join
* leftOuterJoin
* rightOuterJoin
* groupByKey
* reduceByKey
* combineByKey
* distinct
* intersection
* repartition
* coalesce

There are a few ways to use operations that might cause a shuffle and to still avoid much or all network shuffling.

**2 Examples**
1. reduceByKey running on a pre-partitioned RDD will cause the values to be computed locally, requiring only the final reduced value to be sent from the worker to the driver.
2. join called on two RDDs that are pre-partitioned with the same partitioner and cached on the same machine will casue the join to be computed locally, with no shuffling across the networks.

### Shuffles Happen: Key Take away

**How your data is organised on the cluster, and what operations you're doing with it matters!**

We've seen speedups of up to 10 times on small examples just by trying to ensure that data is not transmitted over the network to other machines.

This can hugely affect your day job if you're trying to run a job that should run in 4 hours, but due to missed opportunities to partition data or optimise away a shuffle, it cold take 40 hours instead.

---
## Wide vs Narrow Dependencies

 **Some transformations significantly more expensive (latency) than others**
 *Eg. requiring lots of data to be transferred over the network, sometimes unnecessarily*

 In the past sessions:
 * we learned that shuffling sometimes happens on some transformations.

 In this session:

* we'll look at how RDDs are represented.
* we'll dive into how and when Spark decides it must shuffle data.
* we'll see how these dependencies make fault tolerance possible.

### Lineages
Computations on RDDs are represented as **lineage graph**; a Directed Acyclic Graph (DAG) representing the computations done on the RDD.

```
val rdd = sc.textFile(...)
val filtered = rdd.map(...)
                .filter(...)
                .persist()
val count = filtered.count()
val reduced = filtered.reduce(...)  
```
![dag 1](./resources/dag-1.png)

**Spark represents RDDs in terms of these lineage graphs/DAGs**
*In fact, this is the representation/DAG is what Spark analysis to do optimisations*

### How are RDDs represented?

RDDs are made up of 2 important parts.
(but are made up of 4 parts in total)

RDDs are represented as:
* Partitions. Atomic pieces of the dataset. One or many per compute node

![dag 2](./resources/dag-2.png)
* Dependencies. Models relationship between this RDD and its partitions with the RDD9s) it was derived from.

![dag 2](./resources/dag-3.png)
* A function for computing the dataset based on its parent RDDs.

![dag 2](./resources/dag-4.png)
* Metadata about its partitioning scheme and data placement.

Previously, we arrived at the following rule of thumb for trying to determine when a shuffle might occur:

> **Rule of thumb:** a shuffle can occur when the resulting RDD depends on other elements from the same RDD or antoher RDD.

In fact, RDD dependencies encode when data must move across the network.

**Transformations casue shuffles.** Transformations can have two kinds of dependencies:
1. Narrow Dependencies
2. Wide Dependencies

### Narrow Dependencies vs Wide Dependencies

**Narrow Dependencies**
Each partition of the parent RDD is ued by at most one partition of the child RDD.
*Fast! No shuffle necessary. Optimisations like pipelining possible.*

![narrow dependencies](./resources/narrow-dep-1.png)

 **Wide Dependencies**
 Each paritition of the parent RDD may be depeneded on by multiple child partitions.
*Slow! Requires all or some data to be shuffled over the network.*

![wide dependencies](./resources/wide-dep-1.png)

![wide narrow dependencies](./resources/wide-narrow-dep-1.png)

Since G would be derived from B, which itself is derived from a groupBy and a shuffle on A, you could imagine that we will have already co-partitioned and cached B in memory following the call to groupBy.

**Part of this join is thus a narrow transformation.**

![wide narrow dependencies](./resources/wide-narrow-dep-2.png)


### Which transformations have which kind of dependency?

**Transformatons with narrow dependencies:**
* mapValues
* mapValues
* flatMap
* filter
* mapPartitions
* mapPartitionsWithIndex

**Transformations with wide dependencies:**
(might cause a shuffle)
* cogroup
* groupWith
* join
* leftOuterJoin
* rightOuterJoin
* groupByKey
* reduceByKey
* combineByKey
* distinct
* intersection
* repartition
* coalesce

### How can I find out?

**dependencies** methods on RDDs

dependencies returns a sequence of Dependency objects, which are actually the dependencies used by Spark's scheduler to know how this RDD depends on other RDDs.

The sorts of dependency objects the dependencies method may return include

**Narrow dependecy objects:**
* OneToOneDependency
* PruneDependency
* RangeDependency

**Wide dependency objects:**
* ShuffleDependency

![wide narrow dependecy](./resources/wide-narrow-dep-3.png)


**toDebugString** method on RDDs.

toDebugString prints out a visualistaion of the RDD's lineage, and other information pertinent to scheduling. For example, indentations in the output separate groups of narrow transformations that may be pipelined called stages.

![wide narrow dependecy](./resources/wide-narrow-dep-4.png)

### Lineages and Fault Tolerance

**Lineage graphs are the key to fault tolerance in Spark.**
Ideas from functional programming enable fault tolerance in Spark

* RDDs are immutable.
* We use higher-order functions like map, filter, flatMap to do functional transformations on this immutable data.
* A function for computing the dataset based on its parent RDDs also is part of an RDD's representation

**Along with keeping track of dependency information between partitions as well, this allows us to:**

*Recover from failures by recomputing lost partitions from lineage graphs.*

> Thus we get fault tolerance without the need to write to disk!!

![DAG 5](./resources/dag-5.png)

Recomputing missing partitions fast for narrow dependencies. But slow for wide dependencies!


![DAG 6](./resources/dag-6.png)
