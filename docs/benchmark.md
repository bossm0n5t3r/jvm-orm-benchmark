---
layout: default
title: Benchmark Results
---

# ORM Benchmark

아래 표는 최근 벤치마크 결과입니다.

<!-- benchmark-table:start -->

<table>
<thead>
  <tr>
    <th>Timestamp</th>
    <th>Benchmark</th>
    <th>Benchmark Class</th>
    <th>batchSize</th>
    <th>rows</th>
    <th>Score (s/op)</th>
    <th>Threads</th>
    <th>Forks</th>
    <th>JDK</th>
    <th>Warmup Iters</th>
    <th>Warmup Time</th>
    <th>Warmup Batch</th>
  </tr>
</thead>
<tbody>
<tr>
  <td>2025-11-16 01:07</td>
  <td>insertBulk</td>
  <td>ExposedBenchmarks</td>
  <td>10000</td>
  <td>100000</td>
  <td>51.937517917</td>
  <td>1</td>
  <td>1</td>
  <td>21.0.9</td>
  <td>1</td>
  <td>3 ns</td>
  <td>1</td>
</tr>
<tr>
  <td>2025-11-16 01:07</td>
  <td>insertUsers</td>
  <td>JpaBenchmarks</td>
  <td>10000</td>
  <td>100000</td>
  <td>103.455976292</td>
  <td>1</td>
  <td>1</td>
  <td>21.0.9</td>
  <td>1</td>
  <td>3 ns</td>
  <td>1</td>
</tr>
</tbody>
</table>
<!-- benchmark-table:end -->
