---
layout: default
title: Benchmark Results
---

# ORM Benchmark

아래 표는 최근 벤치마크 결과입니다.

<div markdown="1">
<!-- benchmark-table:start -->

| Timestamp | Benchmark | Benchmark Class | batchSize | rows | Score (s/op) | Threads | Forks | JDK | Warmup Iters | Warmup Time | Warmup Batch |
|-----------|-----------|-----------------|-----------|------|--------------|---------|-------|-----|--------------|-------------|--------------|
| 2025-11-16 00:13 | insertBulk | ExposedBenchmarks | 10000 | 100000 | 53.259007625 | 1 | 1 | 21.0.9 | 1 | 3 ns | 1 |
| 2025-11-16 00:13 | insertUsers | JpaBenchmarks | 10000 | 100000 | 102.965185125 | 1 | 1 | 21.0.9 | 1 | 3 ns | 1 |
<!-- benchmark-table:end -->
</div>
