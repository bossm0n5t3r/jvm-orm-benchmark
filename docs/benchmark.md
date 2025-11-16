# ORM Benchmark

아래 표는 최근 벤치마크 결과입니다.

<!-- orm-versions:start -->

<p>
  JPA (Hibernate): <code>7.1.7.Final</code><br/>
  Exposed: <code>1.0.0-rc-3</code>
</p>

<!-- orm-versions:end -->

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
  <td>2025-11-16 17:00</td>
  <td>insertBulk</td>
  <td>ExposedBenchmarks</td>
  <td>10000</td>
  <td>100000</td>
  <td>54.054799</td>
  <td>1</td>
  <td>1</td>
  <td>21.0.9</td>
  <td>1</td>
  <td>3 ns</td>
  <td>1</td>
</tr>
<tr>
  <td>2025-11-16 17:00</td>
  <td>insertUsers</td>
  <td>JpaBenchmarks</td>
  <td>10000</td>
  <td>100000</td>
  <td>103.698902541</td>
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
