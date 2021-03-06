/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vertx.groovy.core.http

import org.vertx.java.core.Handler
import org.vertx.java.core.http.WebSocketVersion
import org.vertx.java.core.http.SSLConfigureInterceptor

/**
 * An HTTP client that maintains a pool of connections to a specific host, at a specific port. The client supports
 * pipelining of requests.<p>
 * As well as HTTP requests, the client can act as a factory for {@code WebSocket websockets}.<p>
 * If an instance is instantiated from an event loop then the handlers
 * of the instance will always be called on that same event loop.
 * If an instance is instantiated from some other arbitrary Java thread then
 * and event loop will be assigned to the instance and used when any of its handlers
 * are called.<p>
 * Instances cannot be used from worker verticles
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
abstract class HttpClient {
  
  protected org.vertx.java.core.http.HttpClient jClient

  /**
   * Set an exception handler
   */
  void exceptionHandler(Closure handler) {
    jClient.exceptionHandler(handler as Handler)
  }

  /**
   * Set the maximum pool size<p>
   * The client will maintain up to {@code maxConnections} HTTP connections in an internal pool<p>
   * @return A reference to this, so multiple invocations can be chained together.
   */
  HttpClient setMaxPoolSize(int maxConnections) {
    jClient.setMaxPoolSize(maxConnections)
    this
  }

  /**
   * Returns the maximum number of connections in the pool
   */
  int getMaxPoolSize() {
    jClient.getMaxPoolSize()
  }

  /**
   * If {@code keepAlive} is {@code true} then, after the request has ended the connection will be returned to the pool
   * where it can be used by another request. In this manner, many HTTP requests can be pipe-lined over an HTTP connection.
   * Keep alive connections will not be closed until the {@link #close() close()} method is invoked.<p>
   * If {@code keepAlive} is {@code false} then a new connection will be created for each request and it won't ever go in the pool,
   * the connection will closed after the response has been received. Even with no keep alive,
   * the client will not allow more than {@link #getMaxPoolSize()} connections to be created at any one time. <p>
   * @return A reference to this, so multiple invocations can be chained together.
   */
  HttpClient setKeepAlive(boolean keepAlive) {
    jClient.setKeepAlive(keepAlive)
    this
  }

  /**
   * Set the port that the client will attempt to connect to the server on to {@code port}. The default value is
   * {@code 80}
   * @return A reference to this, so multiple invocations can be chained together.
   */
  HttpClient setPort(int port) {
    jClient.setPort(port)
    this
  }

  /**
   * Set the host that the client will attempt to connect to the server on to {@code host}. The default value is
   * {@code localhost}
   * @return A reference to this, so multiple invocations can be chained together.
   */
  HttpClient setHost(String host) {
    jClient.setHost(host)
    this
  }

  /**
   * Attempt to connect an HTML5 websocket to the specified URI<p>
   * The connect is done asynchronously and {@code wsConnect} is called back with the websocket
   */
  void connectWebsocket(String uri, Closure handler) {
    connectWebsocket(uri, WebSocketVersion.RFC6455, handler)
  }

  /**
   * Attempt to connect an HTML5 websocket to the specified URI<p>
   * This version of the method allows you to specify the websockets version using the {@code wsVersion parameter}
   * The connect is done asynchronously and {@code wsConnect} is called back with the websocket
   */
  void connectWebsocket(String uri, WebSocketVersion version, Closure handler) {
    jClient.connectWebsocket(uri, version, {handler(new WebSocket(it))} as Handler)
  }

  /**
   * This is a quick version of the {@link #get(String, Closure)}
   * method where you do not want to do anything with the request before sending.<p>
   * Normally with any of the HTTP methods you create the request then when you are ready to send it you call
   * {@link HttpClientRequest#end()} on it. With this method the request is immediately sent.<p>
   * When an HTTP response is received from the server the {@code responseHandler} is called passing in the response.
   */
  void getNow(String uri, Closure responseHandler) {
    jClient.getNow(uri, wrapResponseHandler(responseHandler))
  }

  /**
   * This method works in the same manner as {@link #getNow(String,Closure)},
   * except that it allows you specify a set of {@code headers} that will be sent with the request.
   */
  void getNow(String uri, Map<String, ? extends Object> headers, Closure responseHandler) {
    jClient.getNow(uri, headers, wrapResponseHandler(responseHandler))
  }

  /**
   * This method returns an {@link HttpClientRequest} instance which represents an HTTP OPTIONS request with the specified {@code uri}.<p>
   * When an HTTP response is received from the server the {@code responseHandler} is called passing in the response.
   */
  HttpClientRequest options(String uri, Closure responseHandler) {
    new HttpClientRequest(jClient.options(uri, wrapResponseHandler(responseHandler)))
  }

  /**
   * This method returns an {@link HttpClientRequest} instance which represents an HTTP GET request with the specified {@code uri}.<p>
   * When an HTTP response is received from the server the {@code responseHandler} is called passing in the response.
   */
  HttpClientRequest get(String uri, Closure responseHandler) {
    request("GET", uri, responseHandler)
  }

  /**
   * This method returns an {@link HttpClientRequest} instance which represents an HTTP HEAD request with the specified {@code uri}.<p>
   * When an HTTP response is received from the server the {@code responseHandler} is called passing in the response.
   */
  HttpClientRequest head(String uri, Closure responseHandler) {
    request("HEAD", uri, responseHandler)
  }

  /**
   * This method returns an {@link HttpClientRequest} instance which represents an HTTP POST request with the specified {@code uri}.<p>
   * When an HTTP response is received from the server the {@code responseHandler} is called passing in the response.
   */
  HttpClientRequest post(String uri, Closure responseHandler) {
    request("POST", uri, responseHandler)
  }

  /**
   * This method returns an {@link HttpClientRequest} instance which represents an HTTP PUT request with the specified {@code uri}.<p>
   * When an HTTP response is received from the server the {@code responseHandler} is called passing in the response.
   */
  HttpClientRequest put(String uri, Closure responseHandler) {
    request("PUT", uri, responseHandler)
  }

  /**
   * This method returns an {@link HttpClientRequest} instance which represents an HTTP DELETE request with the specified {@code uri}.<p>
   * When an HTTP response is received from the server the {@code responseHandler} is called passing in the response.
   */
  HttpClientRequest delete(String uri, Closure responseHandler) {
    request("DELETE", uri, responseHandler)
  }

  /**
   * This method returns an {@link HttpClientRequest} instance which represents an HTTP TRACE request with the specified {@code uri}.<p>
   * When an HTTP response is received from the server the {@code responseHandler} is called passing in the response.
   */
  HttpClientRequest trace(String uri, Closure responseHandler) {
    request("TRACE", uri, responseHandler)
  }

  /**
   * This method returns an {@link HttpClientRequest} instance which represents an HTTP CONNECT request with the specified {@code uri}.<p>
   * When an HTTP response is received from the server the {@code responseHandler} is called passing in the response.
   */
  HttpClientRequest connect(String uri, Closure responseHandler) {
    request("CONNECT", uri, responseHandler)
  }

  /**
   * This method returns an {@link HttpClientRequest} instance which represents an HTTP PATCH request with the specified {@code uri}.<p>
   * When an HTTP response is received from the server the {@code responseHandler} is called passing in the response.
   */
  HttpClientRequest patch(String uri, Closure responseHandler) {
    request("PATCH", uri, responseHandler)
  }

  /**
   * This method returns an {@link HttpClientRequest} instance which represents an HTTP request with the specified {@code uri}. The specific HTTP method
   * (e.g. GET, POST, PUT etc) is specified using the parameter {@code method}<p>
   * When an HTTP response is received from the server the {@code responseHandler} is called passing in the response.
   */
  HttpClientRequest request(String method, String uri, Closure responseHandler) {
    new HttpClientRequest(jClient.request(method, uri, wrapResponseHandler(responseHandler)))
  }

  /**
   * Close the HTTP client. This will cause any pooled HTTP connections to be closed.
   */
  void close() {
    jClient.close()
  }

  /**
   * If {@code ssl} is {@code true}, this signifies that any connections will be SSL connections.
   * @return A reference to this, so multiple invocations can be chained together.
   */
  HttpClient setSSL(boolean ssl) {
    jClient.setSSL(ssl)
    this
  }

  /**
   * Set the path to the SSL key store. This method should only be used in SSL mode, i.e. after {@link #setSSL(boolean)}
   * has been set to {@code true}.<p>
   * The SSL key store is a standard Java Key Store, and will contain the client certificate. Client certificates are
   * only required if the server requests client authentication.<p>
   * @return A reference to this, so multiple invocations can be chained together.
   */
  HttpClient setKeyStorePath(String path) {
    jClient.setKeyStorePath(path)
    this
  }

  /**
   * Set the password for the SSL key store. This method should only be used in SSL mode, i.e. after {@link #setSSL(boolean)}
   * has been set to {@code true}.<p>
   * @return A reference to this, so multiple invocations can be chained together.
   */
  HttpClient setKeyStorePassword(String pwd) {
    jClient.setKeyStorePassword(pwd)
    this
  }

  /**
   * Set the path to the SSL trust store. This method should only be used in SSL mode, i.e. after {@link #setSSL(boolean)}
   * has been set to {@code true}.<p>
   * The trust store is a standard Java Key Store, and should contain the certificates of any servers that the client trusts.
   * If you wish the client to trust all server certificates you can use the {@link #setTrustAll(boolean)} method.<p>
   * @return A reference to this, so multiple invocations can be chained together.
   */
  HttpClient setTrustStorePath(String path) {
    jClient.setTrustStorePath(path)
    this
  }

  /**
   * Set the password for the SSL trust store. This method should only be used in SSL mode, i.e. after {@link #setSSL(boolean)}
   * has been set to {@code true}.<p>
   * @return A reference to this, so multiple invocations can be chained together.
   */
  HttpClient setTrustStorePassword(String pwd) {
    jClient.setTrustStorePassword(pwd)
    this
  }

  /**
   * If you want an SSL client to trust *all* server certificates rather than match them
   * against those in its trust store, you can set this to true.<p>
   * Use this with caution as you may be exposed to "main in the middle" attacks
   * @param trustAll Set to true if you want to trust all server certificates
   */
  HttpClient setTrustAll(boolean trustAll) {
    jClient.setTrustAll(trustAll)
    this
  }

  HttpClient setSslConfigureInterceptor( SSLConfigureInterceptor cfg ) {
	  jClient.setSslConfigureInterceptor(cfg)
	  this
  }

  /**
   * If {@code tcpNoDelay} is set to {@code true} then <a href="http://en.wikipedia.org/wiki/Nagle's_algorithm">Nagle's algorithm</a>
   * will turned <b>off</b> for the TCP connections created by this instance.
   * @return a reference to this so multiple method calls can be chained together
   */
  HttpClient setTCPNoDelay(boolean tcpNoDelay) {
    jClient.setTCPNoDelay(tcpNoDelay)
    this
  }

  /**
   * Set the TCP send buffer size for connections created by this instance to {@code size} in bytes.
   * @return a reference to this so multiple method calls can be chained together
   */
  HttpClient setSendBufferSize(int size) {
    jClient.setSendBufferSize(size)
    this
  }

  /**
   * Set the TCP receive buffer size for connections created by this instance to {@code size} in bytes.
   * @return a reference to this so multiple method calls can be chained together
   */
  HttpClient setReceiveBufferSize(int size) {
    jClient.setReceiveBufferSize(size)
    this
  }

  /**
   * Set the TCP keepAlive setting for connections created by this instance to {@code keepAlive}.
   * @return a reference to this so multiple method calls can be chained together
   */
  HttpClient setTCPKeepAlive(boolean keepAlive) {
    jClient.setTCPKeepAlive(keepAlive)
  }

  /**
   * Set the TCP reuseAddress setting for connections created by this instance to {@code reuse}.
   * @return a reference to this so multiple method calls can be chained together
   */
  HttpClient setReuseAddress(boolean reuse) {
    jClient.setReuseAddress(reuse)
    this
  }

  /**
   * Set the TCP soLinger setting for connections created by this instance to {@code linger}.
   * @return a reference to this so multiple method calls can be chained together
   */
  HttpClient setSoLinger(boolean linger) {
    jClient.setSoLinger(linger)
    this
  }

  /**
   * Set the TCP trafficClass setting for connections created by this instance to {@code trafficClass}.
   * @return a reference to this so multiple method calls can be chained together
   */
  HttpClient setTrafficClass(int trafficClass) {
    jClient.setTrafficClass(trafficClass)
    this
  }

  /**
   * Set the connect timeout in milliseconds
   * @return a reference to this so multiple method calls can be chained together
   */
  HttpClient setConnectTimeout(long timeout) {
    jClient.setConnectTimeout(timeout)
    this
  }

  /**
   * Set the number of boss threads to use. Boss threads are used to make connections.
   * @return a reference to this so multiple method calls can be chained together
   */
  HttpClient setBossThreads(long threads) {
    jClient.setBossThreads(threads)
    this
  }

  /**
   * @return true if Nagle's algorithm is disabled.
   */
  Boolean isTCPNoDelay() {
    jClient.isTCPNoDelay()
  }

  /**
   * @return The TCP send buffer size
   */
  Integer getSendBufferSize() {
    jClient.getSendBufferSize()
  }

  /**
   * @return The TCP receive buffer size
   */
  Integer getReceiveBufferSize() {
    jClient.getReceiveBufferSize()
  }

  /**
   *
   * @return true if TCP keep alive is enabled
   */
  Boolean isTCPKeepAlive() {
    return jClient.isTCPKeepAlive()
  }

  /**
   *
   * @return The value of TCP reuse address
   */
  Boolean isReuseAddress() {
    jClient.isReuseAddress()
  }

  /**
   *
   * @return the value of TCP so linger
   */
  Boolean isSoLinger() {
    jClient.isSoLinger()
  }

  /**
   *
   * @return the value of TCP traffic class
   */
  Integer getTrafficClass() {
    jClient.getTrafficClass()
  }

  /**
   *
   * @return The connect timeout
   */
  Long getConnectTimeout() {
    jClient.getConnectTimeout()
  }

  /**
   *
   * @return The number of boss threads
   */
  Integer getBossThreads() {
    jClient.getBossThreads();
  }

  /**
   *
   * @return true if this client will make SSL connections
   */
  boolean isSSL() {
    jClient.isSSL()
  }

  /**
   *
   * @return true if this client will trust all server certificates.
   */
  boolean isTrustAll() {
    jClient.isTrustAll()
  }

  /**
   *
   * @return The path to the key store
   */
  String getKeyStorePath() {
    jClient.getKeyStorePath()
  }

  /**
   *
   * @return The keystore password
   */
  String getKeyStorePassword() {
    jClient.getKeyStorePassword()
  }

  /**
   *
   * @return The trust store path
   */
  String getTrustStorePath() {
     jClient.getTrustStorePath()
  }

  /**
   *
   * @return The trust store password
   */
  String getTrustStorePassword() {
    jClient.getTrustStorePassword()
  }
  
  private Handler wrapResponseHandler(Closure handler) {
    return {handler(new HttpClientResponse(it))} as Handler
  }  

}
