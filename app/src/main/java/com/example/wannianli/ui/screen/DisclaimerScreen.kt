package com.example.wannianli.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DISCLAIMER_TEXT = """
【重要提示：请仔细阅读本免责声明】

本应用（万年历）是一款基于中国传统历法文化的工具型软件，所有功能（包括但不限于择日、四柱八字、黄道黑道、通天窍、乌兔太阳太阴、本命相主、三垣星官等）均来源于中国传统民俗文化研究，仅供学习、研究、交流之用。

一、文化研究定位
本应用提供的所有历法计算、吉凶推断、时辰选择等信息，均基于传统古籍算法和民俗文化理论，属于传统文化研究范畴，不构成任何形式的预测、占卜或迷信行为。

二、非决策依据
本应用输出的任何信息（包括但不限于吉日推荐、时辰吉凶、星象分析等）均不得作为以下用途的决策依据：
（1）婚丧嫁娶、乔迁开业等重要生活决策；
（2）医疗健康、投资理财、法律事务等专业领域决策；
（3）任何可能产生法律后果的行为决策。

三、责任豁免
用户使用本应用即表示已充分理解并同意：
（1）本应用开发者不对因使用或信赖本应用信息而产生的任何直接或间接损失承担责任；
（2）用户应自行判断信息的适用性，并独立承担使用后果；
（3）本应用有权在不另行通知的情况下修改功能和内容。

四、合规声明
本应用严格遵守《中华人民共和国网络安全法》《互联网信息服务管理办法》等相关法律法规，不传播封建迷信内容，坚持传统文化研究交流定位。

五、知识产权
本应用中的算法逻辑、代码实现、界面设计均受知识产权保护，未经授权不得复制、修改或用于商业用途。

如您不同意以上条款，请立即停止使用本应用并卸载。
""".trimIndent()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisclaimerScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("免责声明", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF3E0)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "本免责声明具有法律约束力，使用本应用即视为您已阅读、理解并同意本声明全部条款。如不同意，请立即停止使用。",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFE65100)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = DISCLAIMER_TEXT,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "已确认同意",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "您已在首次启动时勾选同意本免责声明",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}