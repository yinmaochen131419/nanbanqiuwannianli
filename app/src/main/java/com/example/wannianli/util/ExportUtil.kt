package com.example.wannianli.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.wannianli.data.model.DiaryNote
import java.io.File
import java.io.FileOutputStream

object ExportUtil {

    private const val PACKAGE_WECHAT = "com.tencent.mm"
    private const val PACKAGE_QQ = "com.tencent.mobileqq"
    fun exportNotes(
        context: Context,
        notes: List<DiaryNote>,
        format: ExportFormat,
        label: String
    ): File {
        return when (format) {
            ExportFormat.TXT -> exportTxt(context, notes, label)
            ExportFormat.WORD -> exportWordHtml(context, notes, label)
            ExportFormat.EXCEL -> exportCsv(context, notes, label)
        }
    }

    fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "导出日课笔记"))
    }

    fun shareToWeChat(context: Context, file: File, mimeType: String): Boolean {
        if (!isAppInstalled(context, PACKAGE_WECHAT)) {
            Toast.makeText(context, "未安装微信", Toast.LENGTH_SHORT).show()
            return false
        }
        return try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                setPackage(PACKAGE_WECHAT)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "微信分享失败，请尝试其他分享方式", Toast.LENGTH_SHORT).show()
            false
        }
    }

    fun shareToQQ(context: Context, file: File, mimeType: String): Boolean {
        if (!isAppInstalled(context, PACKAGE_QQ)) {
            Toast.makeText(context, "未安装QQ", Toast.LENGTH_SHORT).show()
            return false
        }
        return try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                setPackage(PACKAGE_QQ)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "QQ分享失败，请尝试其他分享方式", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun isAppInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun exportTxt(context: Context, notes: List<DiaryNote>, label: String): File {
        val file = File(context.getExternalFilesDir(null), "日课笔记_${label}.txt")
        FileOutputStream(file).use { out ->
            out.write("\uFEFF".toByteArray(Charsets.UTF_8))
            val sb = StringBuilder()
            sb.appendLine("═══════════════════════════════════")
            sb.appendLine("        日课应验笔记 ($label)")
            sb.appendLine("═══════════════════════════════════")
            sb.appendLine()
            for ((i, note) in notes.withIndex()) {
                sb.appendLine("━━━ 第 ${i + 1} 条 ━━━")
                sb.appendLine("日期：${note.date}")
                sb.appendLine("时间：${note.time}")
                sb.appendLine("年柱：${note.yearPillar}")
                sb.appendLine("月柱：${note.monthPillar}")
                sb.appendLine("日柱：${note.dayPillar}")
                sb.appendLine("时柱：${note.hourPillar}")
                sb.appendLine("记录：${note.content}")
                sb.appendLine()
            }
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }
        return file
    }

    private fun exportWordHtml(context: Context, notes: List<DiaryNote>, label: String): File {
        val file = File(context.getExternalFilesDir(null), "日课笔记_${label}.doc")
        FileOutputStream(file).use { out ->
            out.write("\uFEFF".toByteArray(Charsets.UTF_8))
            val sb = StringBuilder()
            sb.appendLine("<html><head><meta charset=\"UTF-8\">")
            sb.appendLine("<style>")
            sb.appendLine("body{font-family:'Microsoft YaHei',SimSun,sans-serif;padding:20px;}")
            sb.appendLine("h1{text-align:center;font-size:18pt;}")
            sb.appendLine(".note{border-bottom:1px solid #ccc;padding:10px 0;margin:10px 0;}")
            sb.appendLine(".note-header{font-weight:bold;color:#333;margin-bottom:6px;}")
            sb.appendLine(".pillars{color:#555;margin:4px 0;}")
            sb.appendLine(".content{color:#000;margin-top:6px;white-space:pre-wrap;}")
            sb.appendLine("</style></head><body>")
            sb.appendLine("<h1>日课应验笔记 ($label)</h1>")

            for ((i, note) in notes.withIndex()) {
                sb.appendLine("<div class=\"note\">")
                sb.appendLine("<div class=\"note-header\">第 ${i + 1} 条 —— ${note.date} ${note.time}</div>")
                sb.appendLine("<div class=\"pillars\">年柱：${note.yearPillar}　月柱：${note.monthPillar}　日柱：${note.dayPillar}　时柱：${note.hourPillar}</div>")
                if (note.content.isNotBlank()) {
                    sb.appendLine("<div class=\"content\">${note.content.replace("\n", "<br>")}</div>")
                }
                sb.appendLine("</div>")
            }

            sb.appendLine("</body></html>")
            out.write(sb.toString().toByteArray(Charsets.UTF_8))
        }
        return file
    }

    private fun exportCsv(context: Context, notes: List<DiaryNote>, label: String): File {
        val file = File(context.getExternalFilesDir(null), "日课笔记_${label}.csv")
        FileOutputStream(file).use { out ->
            out.write("\uFEFF".toByteArray(Charsets.UTF_8))
            out.write("序号,日期,时间,年柱,月柱,日柱,时柱,记录\n".toByteArray(Charsets.UTF_8))
            for ((i, note) in notes.withIndex()) {
                val content = note.content.replace("\"", "\"\"")
                val line = "${i + 1},${note.date},${note.time},${note.yearPillar},${note.monthPillar},${note.dayPillar},${note.hourPillar},\"$content\"\n"
                out.write(line.toByteArray(Charsets.UTF_8))
            }
        }
        return file
    }

    enum class ExportFormat(val label: String) {
        TXT("TXT文本"),
        WORD("Word文档"),
        EXCEL("Excel表格")
    }
}