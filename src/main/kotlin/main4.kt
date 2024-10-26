import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import org.xml.sax.InputSource
import java.io.StringReader
import org.w3c.dom.Document

fun main() {
    val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val soapRequest = """
        <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:web="https://api.sukl.sk/">
           <soapenv:Header/>
           <soapenv:Body>
              <web:GetZoznamLiekov/>
           </soapenv:Body>
        </soapenv:Envelope>
    """.trimIndent()

    val mediaType = "text/xml; charset=utf-8".toMediaTypeOrNull()
    val body = soapRequest.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://api.sukl.sk/Webservice2.asmx")
        .post(body)
        .addHeader("SOAPAction", "https://api.sukl.sk/GetZoznamLiekov2")
        .addHeader("Content-Type", "text/xml; charset=utf-8")
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        val responseData = response.body?.string()

        if (responseData != null) {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val inputSource = InputSource(StringReader(responseData))
            val doc: Document = builder.parse(inputSource)

            val xpath = XPathFactory.newInstance().newXPath()

            val lieky = xpath.evaluate("//web_liek", doc, XPathConstants.NODESET) as org.w3c.dom.NodeList

            val file = File("liekyNAJNOVSIE.csv")
            file.printWriter().use { out ->
                out.println("lie_id,lie_kod,lie_nazov,lie_doplnok,drz_nazov,atc_kod,atc_nazov,vyd_nazov,forma,balenie,krajina,reg_cislo,platnost,kategoria,vyslo_datum")

                for (i in 0 until lieky.length) {
                    val liek = lieky.item(i)

                    val lieId = xpath.evaluate("lie_id", liek).ifBlank { "N/A" }
                    val lieKod = xpath.evaluate("lie_kod", liek).ifBlank { "N/A" }
                    val lieNazov = xpath.evaluate("lie_nazov", liek).ifBlank { "N/A" }
                    val lieDoplnok = xpath.evaluate("lie_doplnok", liek).ifBlank { "N/A" }
                    val drzNazov = xpath.evaluate("drz_nazov", liek).ifBlank { "N/A" }
                    val atcKod = xpath.evaluate("atc_kod", liek).ifBlank { "N/A" }
                    val atcNazov = xpath.evaluate("atc_nazov", liek).ifBlank { "N/A" }
                    val vydNazov = xpath.evaluate("vyd_nazov", liek).ifBlank { "N/A" }
                    val forma = xpath.evaluate("forma", liek).ifBlank { "N/A" }
                    val balenie = xpath.evaluate("balenie", liek).ifBlank { "N/A" }
                    val krajina = xpath.evaluate("krajina", liek).ifBlank { "N/A" }
                    val regCislo = xpath.evaluate("reg_cislo", liek).ifBlank { "N/A" }
                    val platnost = xpath.evaluate("platnost", liek).ifBlank { "N/A" }
                    val kategoria = xpath.evaluate("kategoria", liek).ifBlank { "N/A" }
                    val vysloDatum = xpath.evaluate("vyslo_datum", liek).ifBlank { "N/A" }

                    out.println("$lieId,$lieKod,$lieNazov,$lieDoplnok,$drzNazov,$atcKod,$atcNazov,$vydNazov,$forma,$balenie,$krajina,$regCislo,$platnost,$kategoria,$vysloDatum")
                }
            }
            println("Dáta boli zapísané do súboru 'liekyNAJNOVSIE.csv'")
        } else {
            println("Žiadna odpoveď nebola prijatá z API.")
        }
    }
}
