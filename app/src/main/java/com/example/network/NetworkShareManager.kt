package com.example.network

import android.content.Context
import android.util.Log
import com.example.data.MediaAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object NetworkShareManager {
    private const val TAG = "NetworkShareManager"
    const val DEFAULT_SHARE_LABEL = "NAS-Estudio-Fotografia"
    const val EMULATOR_URL = "smb://192.168.50.3/photo"

    // Directories for local mock shares
    fun getShareDirectory(context: Context, shareName: String = "NAS-Estudio-Fotografia"): File {
        val root = context.getExternalFilesDir("network_shares") ?: context.filesDir
        val shareDir = File(root, shareName.replace(" ", "_"))
        if (!shareDir.exists()) {
            shareDir.mkdirs()
        }
        return shareDir
    }

    /**
     * Pre-populates a set of simulated high-end photographic assets (RAW and video formats)
     * if the network directory is newly initialized, to give the user fully functional files.
     */
    suspend fun initializeDefaultAssets(context: Context, connectionId: Int): List<MediaAsset> = withContext(Dispatchers.IO) {
        val shareDir = getShareDirectory(context, DEFAULT_SHARE_LABEL)
        val assets = mutableListOf<MediaAsset>()

        val defaultFiles = listOf(
            MockFileInfo("volcano_andes.NEF", "NEF", "image/x-nikon-nef", "Nikon Z7 II", "f/4.0", "1/400s", 64, "andes, volcan, amanecer", "Volcán Cotopaxi, Ecuador", -0.6806, -78.4361, 5, "6000x4000", 24_500_000L, 1718000000000L),
            MockFileInfo("uyuni_sunset.CR2", "CR2", "image/x-canon-cr2", "Canon EOS R5", "f/2.8", "1/125s", 100, "uyuni, salar, reflejo, nubes", "Salar de Uyuni, Bolivia", -20.1338, -67.4891, 5, "8192x5464", 32_100_000L, 1718100000000L),
            MockFileInfo("tokyo_neon.ARW", "ARW", "image/x-sony-arw", "Sony Alpha 7R V", "f/1.8", "1/80s", 800, "tokyo, neon, lluvia, cyberpunk", "Shibuya, Tokyo, Japan", 35.6580, 139.7016, 4, "9504x6336", 41_800_000L, 1718200000000L),
            MockFileInfo("dji_iceland_aerial.MP4", "MP4", "video/mp4", "DJI Mavic 3 Pro", "f/2.8", "24fps, H.265", 100, "iceland, cascada, drone, cinematográfico", "Skógafoss, Iceland", 63.5320, -19.5113, 5, "3840x2160", 125_400_000L, 1718300000000L),
            MockFileInfo("patagonia_peaks.DNG", "DNG", "image/x-adobe-dng", "Leica Q3", "f/5.6", "1/500s", 100, "patagonia, torres, trekking, nieve", "Torres del Paine, Chile", -50.9423, -72.9360, 4, "9000x6000", 38_200_000L, 1718400000000L),
            MockFileInfo("documental_bosque.MKV", "MKV", "video/x-matroska", "RED Raptor XL", "f/5.6", "60fps, REDCODE", 200, "bosque, sequoia, naturaleza, macro", "Sequoia National Park, USA", 36.4864, -118.5658, 4, "8192x4320", 340_000_000L, 1718500000000L)
        )

        for (info in defaultFiles) {
            val fileObj = File(shareDir, info.name)
            if (!fileObj.exists()) {
                try {
                    // Create simulated empty payload file
                    fileObj.createNewFile()
                    FileOutputStream(fileObj).use { out ->
                        out.write("SIMULATED ${info.ext} CAMERA PAYLOAD".toByteArray())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed creating simulated file ${info.name}: ${e.message}")
                }
            }

            // Create initial standard XMP sidecar file for this asset
            val initialTags = info.tags.split(", ").filter { it.isNotEmpty() }
            writeXmpSidecarFile(fileObj, initialTags, info.location, info.rating)

            // Convert to DB Entity representation
            assets.add(
                MediaAsset(
                    connectionId = connectionId,
                    fileName = info.name,
                    relativePath = "/${info.name}",
                    fileExtension = info.ext,
                    fileSizeBytes = info.sizeBytes,
                    dateTimeOriginal = info.timestamp,
                    tags = info.tags,
                    locationName = info.location,
                    latitude = info.lat,
                    longitude = info.lng,
                    rating = info.rating,
                    xmpSyncStatus = "SYNCED",
                    cloudSyncStatus = "NOT_SYNCED",
                    dimensions = info.dimensions,
                    cameraModel = info.cameraModel,
                    exifAperture = info.aperture,
                    exifShutter = info.shutter,
                    exifIso = info.iso
                )
            )
        }

        return@withContext assets
    }

    /**
     * Generates a fully-compliant standard Adobe XMP Sidecar document contents.
     */
    fun generateXmpContent(fileName: String, tags: List<String>, location: String, rating: Int): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        sb.append("<x:xmpmeta xmlns:x=\"adobe:ns:meta/\" x:xmptk=\"Adobe XMP Core 9.1-c001\">\n")
        sb.append("  <rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">\n")
        sb.append("    <rdf:Description rdf:about=\"\"\n")
        sb.append("        xmlns:dc=\"http://purl.org/dc/elements/1.1/\"\n")
        sb.append("        xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\"\n")
        sb.append("        xmlns:photoshop=\"http://ns.adobe.com/photoshop/1.0/\"\n")
        sb.append("        xmlns:xmpLightroom=\"http://ns.adobe.com/lightroom/1.0/\">\n")
        
        // Tags representation
        if (tags.isNotEmpty()) {
            sb.append("      <dc:subject>\n")
            sb.append("        <rdf:Bag>\n")
            for (tag in tags) {
                sb.append("          <rdf:li>${escapeXml(tag)}</rdf:li>\n")
            }
            sb.append("        </rdf:Bag>\n")
            sb.append("      </dc:subject>\n")
        }

        // Location / Label / Rating representation
        if (location.isNotEmpty()) {
            sb.append("      <photoshop:City>${escapeXml(location)}</photoshop:City>\n")
            sb.append("      <xmp:Label>${escapeXml(location)}</xmp:Label>\n")
        }
        sb.append("      <xmp:Rating>$rating</xmp:Rating>\n")
        sb.append("      <xmp:ModifyDate>${java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).format(java.util.Date())}</xmp:ModifyDate>\n")
        
        sb.append("    </rdf:Description>\n")
        sb.append("  </rdf:RDF>\n")
        sb.append("</x:xmpmeta>")
        return sb.toString()
    }

    /**
     * Physical file writer for XMP sidecar. Output named strictly: original_file_name.xmp
     */
    fun writeXmpSidecarFile(originalFile: File, tags: List<String>, location: String, rating: Int): File? {
        val xmpFile = File(originalFile.parentFile, "${originalFile.name}.xmp")
        val content = generateXmpContent(originalFile.name, tags, location, rating)
        return try {
            xmpFile.writeText(content, Charsets.UTF_8)
            Log.d(TAG, "Successfully wrote XMP sidecar: ${xmpFile.absolutePath}")
            xmpFile
        } catch (e: Exception) {
            Log.e(TAG, "Failed writing XMP file for ${originalFile.name}: ${e.message}")
            null
        }
    }

    /**
     * Parses metadata from an existing XMP sidecar file.
     */
    fun readXmpSidecarFile(xmpFile: File): ParsedXmp {
        if (!xmpFile.exists()) return ParsedXmp(emptyList(), "", 0)
        return try {
            val content = xmpFile.readText(Charsets.UTF_8)
            val tags = mutableListOf<String>()
            
            // Extract dc:subject tag list
            val liRegex = "<rdf:li>(.*?)</rdf:li>".toRegex()
            liRegex.findAll(content).forEach { match ->
                tags.add(unescapeXml(match.groupValues[1]))
            }

            // Extract label/city
            val labelRegex = "<xmp:Label>(.*?)</xmp:Label>".toRegex()
            val cityRegex = "<photoshop:City>(.*?)</photoshop:City>".toRegex()
            val labelMatch = labelRegex.find(content)?.groupValues?.get(1)
            val cityMatch = cityRegex.find(content)?.groupValues?.get(1)
            val location = unescapeXml(labelMatch ?: cityMatch ?: "")

            // Extract rating
            val ratingRegex = "<xmp:Rating>(\\d+)</xmp:Rating>".toRegex()
            val rating = ratingRegex.find(content)?.groupValues?.get(1)?.toIntOrNull() ?: 0

            ParsedXmp(tags, location, rating)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading XMP parse: ${e.message}")
            ParsedXmp(emptyList(), "", 0)
        }
    }

    private fun escapeXml(input: String): String {
        return input.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun unescapeXml(input: String): String {
        return input.replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
    }
}

data class MockFileInfo(
    val name: String,
    val ext: String,
    val mime: String,
    val cameraModel: String,
    val aperture: String,
    val shutter: String,
    val iso: Int,
    val tags: String,
    val location: String,
    val lat: Double,
    val lng: Double,
    val rating: Int,
    val dimensions: String,
    val sizeBytes: Long,
    val timestamp: Long
)

data class ParsedXmp(
    val tags: List<String>,
    val location: String,
    val rating: Int
)
