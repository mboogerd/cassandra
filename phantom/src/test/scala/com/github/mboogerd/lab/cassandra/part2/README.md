# Part 2

The tests in this package are based on the article 

Here, we learn about modelling different cardinalities in Cassandra:

> Modelling one-to-one relationships can be done by using a single PRIMARY / PARTITION KEY.
> Modelling one-to-many relationships can be done by using a COMPOUND KEY.
> Modelling many-to-many relationships can be done by using a COMPOSITE KEY.

In addition, we learn about CLUSTERING ORDER. This can be used to control what column will be used to sort the SSTables with, therefore enabling us to perform efficient range queries on this column.