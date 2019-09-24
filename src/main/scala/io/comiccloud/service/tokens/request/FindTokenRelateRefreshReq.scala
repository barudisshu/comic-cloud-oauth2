package io.comiccloud.service.tokens.request

import io.comiccloud.service.clients.response.ClientResp

case class FindTokenRelateRefreshReq(clientFO: ClientResp, refreshToken: String)
