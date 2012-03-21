This is just a little experiment in generating prototype classes
as prototypes are modified. The idea is to help prototype-based
languages on JVM fit better into the typical patterns of JVM
classes and invokedynamic binding.

The general idea is that for whatever your "object" is in the
language of choice, a prototype class will be carried along with
it. The prototype class will hold the actual data in JVM fields,
which can then be bound directly by invokedynamic. Fields are a
more efficient storage mechanism than an array because the
references usually pack (on 64 bit) and because objects have a
smaller header than arrays.

A simple invokedynamic guard would then be to confirm that the
incoming object's prototype class is the same as the last time,
and if it changes rebind (with some allowance for polymorphism,
shared supertypes, and the like). By this mechanism, the guard
has only a single field access, which is the next cheapest
thing to a class comparison of the target class itself.

Alternative Strategies
----------------------

* Instrumentation to allow redefining running classes as fields
are added.

This would indeed be an elegant way to rewrite the
objects in place, adding new fields as modifications are made,
but I opted not to require instrumentation or changing object
classes directly.

* Arrays bound dynamically by invokedynamic and grown as
necessary.

This mechanism would not require generating new
classes, but as mentioned above arrays are larger than objects
and there would still need to be a mechanism for validating
that the prototype had not changed. So we end up with both
larger storage and at least one additional reference for the
identity of the current prototype.

Known Problems
--------------

* Expensive process of finding already-generated prototype
classes.

* Expensive copy construction (only really escapable by using
instrumentation as mentioned above).
