/**
 * Provides Undertow handlers for advanced logging capabilities, focusing on the concept of a "logging group".
 * A logging group allows multiple related HTTP requests to be logged together as a single logical unit,
 * which is particularly useful for tracking complex user interactions or business transactions that span
 * multiple requests. This package includes handlers for defining, assisting, and managing these logging
 * groups, with support for path-based grouping to automatically associate requests with a group.
 */
package com.aspectran.undertow.server.handler.logging;
