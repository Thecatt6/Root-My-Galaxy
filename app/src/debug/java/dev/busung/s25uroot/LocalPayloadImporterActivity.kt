package dev.busung.s25uroot

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import android.system.Os
import java.io.File
import java.io.FileOutputStream

class LocalPayloadImporterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!BuildConfig.DEBUG) {
            finish()
            return
        }
        try {
            val assetFolder = "payloads/sample-a53"
            val assetManager = assets
            val files = assetManager.list(assetFolder) ?: arrayOf()
            val destDir = File(filesDir, "payloads/sample-a53").apply { mkdirs() }
            val copied = mutableListOf<String>()
            for (name in files) {
                val inStream = assetManager.open("$assetFolder/$name")
                val outFile = File(destDir, name)
                FileOutputStream(outFile).use { out ->
                    inStream.copyTo(out)
                    out.fd.sync()
                }
                inStream.close()
                try {
                    Os.chmod(outFile.absolutePath, 0b111101101) // rwxr-xr-x
                } catch (e: Throwable) {
                    // ignore on platforms without Os.chmod
                }
                copied.add(outFile.absolutePath)
            }
            Toast.makeText(this, "Imported payload: ${copied.size} files", Toast.LENGTH_LONG).show()
        } catch (e: Throwable) {
            Toast.makeText(this, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
        finish()
    }
}
