# ufwj
Java Building Blocks

[![Build Status](https://travis-ci.org/mrbald/ufwj.svg?branch=master)](https://travis-ci.org/mrbald/ufwj)
[![HitCount](_http://hits.dwyl.io/mrbald/ufwj.svg)](http://hits.dwyl.io/mrbald/ufwj)

## Featured Items

### [WatermarkQueue](base/src/main/java/net/bobah/ufwj/queue/WatermarkQueue.java)
A queue with embedded lower/upper watermarks tracker and callbacks functionality.
Usable for monitoring or implementing soft back pressure.
Implemented as a workaround for MINA integration bug in QuickFIXj.

### [FixedCapOpenHash](base/src/main/java/net/bobah/ufwj/hash/FixedCapOpenHash.java)
A fixed capacity hash map with open addressing.
Internally is using power-of-two capacity plain arrays as a storage in a ring-buffer-ish way.
Should be slightly better than the JDK version for fixed key sets of small size.
Implemented for parallel correlated request-response tracking with known maximum number of concurrent parallel requests.
