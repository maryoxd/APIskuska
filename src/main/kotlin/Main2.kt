import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.w3c.dom.Document
import java.io.File
import java.io.IOException
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import org.xml.sax.InputSource
import java.io.StringReader
import java.util.concurrent.TimeUnit


fun main() {
    // Nastavenie timeoutov pre OkHttp klienta
    val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // Nastavenie timeoutu pre pripojenie
        .writeTimeout(60, TimeUnit.SECONDS) // Nastavenie timeoutu pre zápis
        .readTimeout(60, TimeUnit.SECONDS) // Nastavenie timeoutu pre čítanie
        .build()

    // SOAP request body
    val soapRequest = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="https://api.sukl.sk/">
           <soapenv:Header/>
           <soapenv:Body>
              <web:LiekyAtc/>
           </soapenv:Body>
        </soapenv:Envelope>
    """.trimIndent()

    // Vytvorenie RequestBody pomocou toRequestBody
    val mediaType = "text/xml; charset=utf-8".toMediaTypeOrNull()
    val body: RequestBody = soapRequest.toRequestBody(mediaType)

    // Vytvorenie requestu
    val request = Request.Builder()
        .url("https://api.sukl.sk/Webservice2.asmx")
        .post(body)
        .addHeader("SOAPAction", "https://api.sukl.sk/LiekyAtc")
        .addHeader("Content-Type", "text/xml; charset=utf-8")
        .build()

    // Vykonanie requestu
    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val responseData = response.body?.string()

        if (responseData != null) {
            // Parsovanie XML pomocou DOM parsera
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val inputSource = InputSource(StringReader(responseData))
            val doc: Document = builder.parse(inputSource)

            // XPath pre extrakciu potrebných polí
            val xpath = XPathFactory.newInstance().newXPath()

            // XPath query na získanie elementov atczoznam
            val atcList = xpath.evaluate("//atczoznam", doc, XPathConstants.NODESET) as org.w3c.dom.NodeList

            // Vytvor súbor CSV
            val file = File("atc_lieky.csv")
            file.printWriter().use { out ->
                // Hlavička CSV súboru
                out.println("atc_kod,atc_nazov,atc_nazov_sk")

                // Prechádzanie cez každý záznam atczoznam
                for (i in 0 until atcList.length) {
                    val atc = atcList.item(i)

                    // Extrakcia hodnôt pomocou XPath
                    val atcKod = xpath.evaluate("atc_kod", atc)
                    val atcNazov = xpath.evaluate("atc_nazov", atc)
                    val atcNazovSk = xpath.evaluate("atc_nazov_sk", atc)

                    // Zapíš jeden riadok do CSV
                    out.println("$atcKod,$atcNazov,$atcNazovSk")
                }
            }
            println("Dáta boli zapísané do súboru 'atc_lieky.csv'")
        } else {
            println("Žiadna odpoveď nebola prijatá z API.")
        }
    }
}