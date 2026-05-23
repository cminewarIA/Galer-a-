package com.example

import org.junit.Test
import java.io.File
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Graphics2D
import java.awt.RenderingHints

class IconGeneratorTest {

    @Test
    fun generateIcons() {
        println("=== INICIANDO GENERADOR DE ICONOS NETGALLERY ===")
        // Resolve paths correctly relative to where test is running
        val possibleSrcPaths = listOf(
            File("src/main/res/drawable/img_app_logo.png"),
            File("app/src/main/res/drawable/img_app_logo.png"),
            File("../app/src/main/res/drawable/img_app_logo.png")
        )
        
        val srcFile = possibleSrcPaths.find { it.exists() }
            ?: throw IllegalStateException("No se pudo encontrar el archivo origen img_app_logo.png. Buscado en: " + 
                possibleSrcPaths.map { it.absolutePath })
        
        println("Imagen origen encontrada en: ${srcFile.absolutePath}")
        val img = ImageIO.read(srcFile)
        
        val parent = srcFile.parentFile ?: throw IllegalStateException("Parent folder not found")
        val baseDir = parent.parentFile ?: throw IllegalStateException("res folder not found")
        
        val sizes = mapOf(
            "mdpi" to 48,
            "hdpi" to 72,
            "xhdpi" to 96,
            "xxhdpi" to 144,
            "xxxhdpi" to 192
        )
        
        sizes.forEach { (density, size) ->
            val mipmapDir = File(baseDir, "mipmap-$density")
            if (!mipmapDir.exists()) {
                mipmapDir.mkdirs()
            }
            
            // Delete old conflicts (WebP, XML, or custom PNGs) to avoid build errors or caching issues
            File(mipmapDir, "ic_launcher.webp").delete()
            File(mipmapDir, "ic_launcher_round.webp").delete()
            File(mipmapDir, "ic_launcher.png").delete()
            File(mipmapDir, "ic_launcher_round.png").delete()
            
            // Resize using premium AWT Graphics2D with Bicubic scaling & complete alpha transparency support
            val resized = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
            val g: Graphics2D = resized.createGraphics()
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.drawImage(img, 0, 0, size, size, null)
            g.dispose()
            
            // Write high-quality PNG launcher files with transparency
            ImageIO.write(resized, "png", File(mipmapDir, "ic_launcher.png"))
            ImageIO.write(resized, "png", File(mipmapDir, "ic_launcher_round.png"))
            println("Generado correctamente: ic_launcher.png (${size}x${size}) en mipmap-$density")
        }
        
        println("=== PROCESO DE GENERACIÓN DE ICONOS COMPLETADO EXITOSAMENTE ===")
    }
}
