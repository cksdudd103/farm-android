package com.smartfarm.app.ui.screens.links

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartfarm.app.ui.components.ScreenHeader
import com.smartfarm.app.ui.components.SectionCard

private data class RelatedSite(val name: String, val description: String, val url: String)

private val relatedSites = listOf(
    RelatedSite("농림축산식품부", "농업 정책 및 지원사업 안내", "https://www.mafra.go.kr"),
    RelatedSite("농촌진흥청", "농업 기술 정보 및 연구 성과", "https://www.rda.go.kr"),
    RelatedSite("농수산물유통공사(aT)", "농산물 유통 및 가격 정보", "https://www.at.or.kr"),
    RelatedSite("농산물 유통정보(KAMIS)", "실시간 농산물 시세 정보", "https://www.kamis.or.kr"),
    RelatedSite("농업기술포털 농사로", "영농기술 및 재배 정보", "https://www.nongsaro.go.kr"),
    RelatedSite("농정원", "농업 정보화 및 스마트팜 지원", "https://www.epis.or.kr"),
    RelatedSite("기상청 농업기상", "농업 특화 기상 정보", "https://www.kma.go.kr"),
    RelatedSite("귀농귀촌종합센터", "귀농·귀촌 지원 정보", "https://www.returnfarm.com"),
)

@Composable
fun LinksScreen() {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "농업 관련 사이트", subtitle = "유용한 농업 관련 기관 사이트 모음")
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(relatedSites) { site ->
                SectionCard(
                    modifier = Modifier.clickable {
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(site.url))
                        context.startActivity(intent)
                    },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(site.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(site.description, style = MaterialTheme.typography.bodyMedium)
                        }
                        Icon(Icons.Filled.OpenInNew, contentDescription = "열기")
                    }
                }
            }
        }
    }
}
