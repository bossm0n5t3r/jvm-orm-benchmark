# ORM Benchmark

아래 표는 최근 벤치마크 결과입니다.

<!-- benchmark-table:start -->

| Benchmark | Benchmark Class | batchSize | rows | Score (s/op) | Threads | Forks | JDK | Warmup Iters | Warmup Time | Warmup Batch |
|-----------|-----------------|-----------|------|--------------|---------|-------|-----|--------------|-------------|--------------|
| insertBulk | ExposedBenchmarks | 10000 | 100000 | 53.194047042 | 1 | 1 | 21.0.9 | 1 | 3 ns | 1 |
| insertUsers | JpaBenchmarks | 10000 | 100000 | 103.185657708 | 1 | 1 | 21.0.9 | 1 | 3 ns | 1 |
<!-- benchmark-table:end -->
