# Part 1

The tests in this package are based on the article [A series on Cassandra – Part 1: Getting rid of the SQL mentality](http://outworkers.com/blog/post/a-series-on-cassandra-part-1-getting-rid-of-the-sql-mentality).

Here, we learn the distinction between "data-to-store" and "data-to-query". Proper use of Cassandra means using the partition-key for queries. Using Cassandra indexes should be evaded as their implementation is orders of magnitude less efficient than using the partition-key. The analogous construction for a secondary-index in idiomatic Cassandra is an application-managed index:

- Construct a second table that maps the attribute to query (its partition-key) to the partition-key(s) of the table containing data you are interested in
- When querying on the new attribute, query the application-maintained index, and chain the result with a call to the original table.
- When updating elements, it is now your responsibility to update this second table, hence why I call it "application-managed-index"