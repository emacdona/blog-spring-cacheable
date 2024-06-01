package com.consartist.spring.cache.data;

import io.swagger.v3.oas.annotations.media.Schema;

public interface CacheMeta {
  @Schema(description = "Which host serviced the request that provided this value?")
  String getHost();

  @Schema(description = "Was this value retrieved from the cache?")
  Boolean getCached();
}
