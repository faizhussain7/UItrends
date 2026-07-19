package com.mfhapps.trendingui.legal

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LegalDocumentRepository {

    suspend fun load(kind: LegalDocumentKind): LegalDocumentUiState = withContext(Dispatchers.IO) {
        if (!kind.isRemote) {
            return@withContext LegalDocumentUiState.Ready(bundledLegalDocument(kind))
        }

        val docId = kind.googleDocId
        if (docId.isNullOrBlank()) {
            return@withContext LegalDocumentUiState.Error(
                title = kind.title,
                message = "This document is not configured yet.",
                viewUrl = null,
            )
        }

        val viewUrl = AppLinks.googleDocViewUrl(docId)
        runCatching {
            val body = httpGet(AppLinks.googleDocExportTxtUrl(docId))
            require(body.isNotBlank()) { "Empty document" }
            GoogleDocsLegalParser.parse(kind = kind, raw = body, viewUrl = viewUrl)
        }.fold(
            onSuccess = { LegalDocumentUiState.Ready(it) },
            onFailure = { error ->
                LegalDocumentUiState.Error(
                    title = kind.title,
                    message = error.message ?: "Could not load document",
                    viewUrl = viewUrl,
                )
            },
        )
    }

    private fun httpGet(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            instanceFollowRedirects = true
            setRequestProperty("Accept", "text/plain,*/*")
            setRequestProperty("User-Agent", "UITrends-Android")
            connectTimeout = 15_000
            readTimeout = 20_000
        }
        try {
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code !in 200..299) {
                error("Unable to load document ($code)")
            }
            return body
        } finally {
            connection.disconnect()
        }
    }
}
