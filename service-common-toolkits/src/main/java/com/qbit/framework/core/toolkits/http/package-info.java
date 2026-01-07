/**
 * HTTP 客户端工具包
 * <p>
 * 提供灵活易用、支持扩展的 HTTP 请求工具类
 * <p>
 * <b>快速开始：</b>
 * <pre>{@code
 * // 简单的GET请求
 * HttpResponse response = HttpUtils.get("https://api.example.com/users");
 * 
 * // POST JSON请求
 * User user = new User("张三", 25);
 * HttpResponse response = HttpUtils.post("https://api.example.com/users", user);
 * 
 * // 带请求头的GET请求
 * Map<String, String> headers = Map.of("Authorization", "Bearer token");
 * HttpResponse response = HttpUtils.get("https://api.example.com/profile", headers);
 * }</pre>
 * 
 * <b>高级用法：</b>
 * <pre>{@code
 * // 使用构建器模式
 * HttpResponse response = HttpUtils.request("https://api.example.com/users")
 *     .post()
 *     .header("Authorization", "Bearer token")
 *     .header("X-Request-Id", UUID.randomUUID().toString())
 *     .queryParam("page", "1")
 *     .queryParam("size", "10")
 *     .jsonBody(user)
 *     .timeout(Duration.ofSeconds(5))
 *     .build();
 * 
 * // 使用自定义客户端
 * HttpClient okHttpClient = HttpUtils.createOkHttpClient();
 * HttpResponse response = HttpUtils.execute(okHttpClient, request);
 * }</pre>
 * 
 * <b>响应处理：</b>
 * <pre>{@code
 * HttpResponse response = HttpUtils.get("https://api.example.com/user/1");
 * if (response.isSuccessful()) {
 *     User user = HttpUtils.parseResponse(response, User.class);
 *     System.out.println(user);
 * }
 * }</pre>
 * 
 * <b>支持的实现：</b>
 * <ul>
 * <li>JdkHttpClientImpl - 基于 JDK 11+ HttpClient（默认）</li>
 * <li>OkHttpClientImpl - 基于 OkHttp（需要添加依赖）</li>
 * </ul>
 * 
 * <b>扩展自定义实现：</b>
 * <pre>{@code
 * public class CustomHttpClient implements HttpClient {
 *     @Override
 *     public HttpResponse execute(HttpRequest request) {
 *         // 自定义实现
 *     }
 * }
 * }</pre>
 *
 * @author zhoubobing
 * @date 2026/1/7
 */
package com.qbit.framework.core.toolkits.http;
