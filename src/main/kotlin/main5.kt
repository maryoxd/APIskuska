import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object HelloWorld {
    @JvmStatic
    fun main(args: Array<String>) {
        try {
            // URL endpointu pre konkrétny Bundle s ID
            val bundleId = "7b020df3-796f-4aad-9c2e-25e052807f4c"  // Tu je ID, ktoré chceme získať
            val urlString = "https://epi.ema.europa.eu/consuming/api/fhir/List/f6b83992-4d1d-419e-95d7-96455e6684d"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            // Request headers
            connection.setRequestProperty("Cache-Control", "no-cache")
            connection.requestMethod = "GET"

            // Získanie HTTP status kódu
            val status = connection.responseCode
            println("HTTP Status Code: $status")

            // Spracovanie odpovede
            val `in` = BufferedReader(
                InputStreamReader(connection.inputStream)
            )
            var inputLine: String?
            val content = StringBuffer()
            while (`in`.readLine().also { inputLine = it } != null) {
                content.append(inputLine)
            }
            `in`.close()

            // Výpis obsahu odpovede
            println("Response Content:")
            println(content)

            // Ukončenie spojenia
            connection.disconnect()
        } catch (ex: Exception) {
            println("Exception occurred: ${ex.message}")
        }
    }
}
