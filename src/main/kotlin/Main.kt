import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.create
import java.io.IOException

fun main() {
    val client = OkHttpClient()

    // SOAP request body
    val soapRequest = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="https://api.sukl.sk/">
           <soapenv:Header/>
           <soapenv:Body>
              <web:GetZoznamLiekov2/>
           </soapenv:Body>
        </soapenv:Envelope>
    """.trimIndent()

    // Set content type for SOAP and create request body
    val mediaType = "text/xml; charset=utf-8".toMediaTypeOrNull()
    val body: RequestBody = create(mediaType, soapRequest)

    // Build the request (bez SOAPAction hlavičky)
    val request = Request.Builder()
        .url("https://api.sukl.sk/Webservice2.asmx")  // Základná URL
        .post(body)
        .addHeader("Content-Type", "text/xml; charset=utf-8")
        .build()

    // Execute the request
    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val responseData = response.body?.string()
        println(responseData) // Zobraz odpoveď z API
    }
}
