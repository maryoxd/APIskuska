import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

fun main() {
    val client = OkHttpClient()

    // SOAP request body
    val soapRequest = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="https://api.sukl.sk/">
           <soapenv:Header/>
           <soapenv:Body>
              <web:GetZoznamZP/>
           </soapenv:Body>
        </soapenv:Envelope>
    """.trimIndent()

    val mediaType = "text/xml; charset=utf-8".toMediaTypeOrNull()
    val body: RequestBody = soapRequest.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://api.sukl.sk/Webservice2.asmx")
        .post(body)
        .addHeader("SOAPAction", "https://api.sukl.sk/GetZoznamZP")
        .addHeader("Content-Type", "text/xml; charset=utf-8")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val responseData = response.body?.string()

        if (responseData != null) {
            // Vypíš celú odpoveď z API
            println("API Response: $responseData")
        } else {
            println("Žiadna odpoveď nebola prijatá z API.")
        }
    }
}
