package qhaty.qqex

import android.app.AlertDialog
import android.content.Context
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.jaredrummler.android.shell.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


fun Context.toast(str: String? = null, id: Int? = null) {
    Toast.makeText(this, str ?: this.resources.getText(id!!), Toast.LENGTH_SHORT).show()
}

fun encodeMD5(text: String): String {
    try {
        val instance: MessageDigest = MessageDigest.getInstance("MD5")
        val digest: ByteArray = instance.digest(text.toByteArray())
        val sb = StringBuffer()
        for (b in digest) {
            //获取低八位有效值
            val i: Int = b.toInt() and 0xff
            //将整数转化为16进制
            var hexString = Integer.toHexString(i)
            if (hexString.length < 2) {
                //如果是一位的话，补0
                hexString = "0$hexString"
            }
            sb.append(hexString)
        }
        return sb.toString()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    }
    return ""
}

suspend fun textToAppDownload(context: Context, fileName: String, text: String) {
    withContext(Dispatchers.IO) {
        val path = context.getExternalFilesDir("Save")
        val file = File("$path/$fileName.html")
        if (file.exists()) file.delete()
        file.createNewFile()
        FileOutputStream(file).write(text.toByteArray())
    }

}

suspend fun textToAppData(context: Context, fileName: String, text: String) {
    withContext(Dispatchers.IO) {
        val path = context.getExternalFilesDir("Data")
        if (path != null) {
            if (!path.exists()) path.mkdirs()
        } else {
            runOnUI { context.toast("无内置储存") }
            return@withContext
        }
        val file = File("${path.absolutePath}/$fileName")
        if (file.exists()) file.delete()
        file.createNewFile()
        file.writeText(text)
    }
}

fun runOnUI(a: () -> Unit) {
    GlobalScope.launch(Dispatchers.Main) { a() }
}

data class Progress(var progress: Int, var msg: String)

class ProgressView {
    companion object {
        var progressView: ProgressBar? = null
        var progressText: TextView? = null
        var dialog: AlertDialog? = null
        var cirProgress: ProgressBar? = null
    }
}

data class CodedChat(var time: Int, var type: Int, var sender: String, var msg: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CodedChat
        if (!msg.contentEquals(other.msg)) return false
        return true
    }

    override fun hashCode(): Int {
        return msg.contentHashCode()
    }
}

data class Chat(var time: Int, var type: Int, var sender: String, var msg: String)

fun getKeyUseRoot(context: Context) {
    if (Data.hasRoot) {
        if (Shell.SU.available()) {
            val dir = context.getExternalFilesDir("qqxml")!!
            val qqPkg = "com.tencent.mobileqq"
            val cmd1 =
                "cp -f /data/data/$qqPkg/shared_prefs/appcenter_mobileinfo.xml ${dir.absolutePath}/1.xml"
            val cmd2 =
                "cp -f /data/data/$qqPkg/shared_prefs/DENGTA_META.xml ${dir.absolutePath}/2.xml"
            Shell.SU.run(cmd1, cmd2)
            val regex0 = Regex("""imei">.*?</""")
            val regex1 = Regex("""ress">.*?</""")
            val regex2 = Regex("""IMEI_DENGTA">.*?</""")
            val file1 = File("${dir.absolutePath}/1.xml")
            val file2 = File("${dir.absolutePath}/2.xml")
            when (Data.keyType) {
                1 -> {
                    val text = file1.readText()
                    Data.key = regex0.find(text)!!.value.replace("""imei">""", "").replace("""</""", "")
                }
                0 -> {
                    val text = file1.readText()
                    Data.key = regex1.find(text)!!.value.replace("""ress">""", "").replace("""</""", "")
                }
                2 -> {
                    val text = file2.readText()
                    Data.key = regex2.find(text)!!.value.replace("""IMEI_DENGTA">""", "").replace("""</""", "")
                }
            }
        }
    }
}
