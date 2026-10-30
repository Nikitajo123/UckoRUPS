package si.uckoquiz.app.games

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import si.uckoquiz.app.domain.Task

/**
 * Sentence Builder (game id "sentence_builder", UQ-49).
 * The player taps shuffled words to build the sentence, then checks it.
 *
 * Content (in the lesson JSON):
 *   "answers": the sentences, one per round, e.g. ["My sister is twelve years old.", "This is my brother."]
 *   "config.hints": optional Slovenian hints, separated by |, same order as the sentences
 *   "config.extra": optional wrong words, separated by commas; one is mixed in each round
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SentenceBuilderGame(task: Task, onFinish: (GameResult) -> Unit) {
    val color = GameColors.forTaskId(task.id)
    val sentences = remember(task.id) {
        task.answers.filter { it.isNotBlank() }.ifEmpty { listOf("My sister is twelve years old.") }
    }
    val hints = remember(task.id) { task.config["hints"]?.split("|")?.map { it.trim() } ?: emptyList() }
    val extras = remember(task.id) {
        task.config["extra"]?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()
    }
    val session = rememberGameSession(total = sentences.size)
    var round by remember(task.id) { mutableIntStateOf(0) }
    var checked by remember(task.id) { mutableStateOf<Boolean?>(null) } // null = not checked yet

    // Show the end screen only after the player has seen the last feedback.
    if (session.isOver && checked == null) {
        val result = remember { session.result() }
        GameEnd(result = result, onContinue = { onFinish(result) })
        return
    }

    val sentence = sentences[round.coerceAtMost(sentences.lastIndex)]
    val words = remember(sentence) { splitWords(sentence) }
    val ending = remember(sentence) { sentence.trim().lastOrNull()?.takeIf { it in ".?!" }?.toString() ?: "" }
    val bank = remember(task.id, round) {
        (words + extras.shuffled().take(1)).mapIndexed { i, w -> WordTile(i, w) }.shuffled()
    }
    val placed = remember(task.id, round) { mutableStateListOf<WordTile>() }

    Column(Modifier.fillMaxSize()) {
        GameHud(
            title = "Sentence Builder",
            session = session,
            color = color,
            onClose = { onFinish(session.result()) },
        )
        Box(
            Modifier
                .fillMaxWidth()
                .background(GameColors.Surface)
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(task.prompt.ifBlank { "Build the sentence." }, fontWeight = FontWeight.Bold)
        }

        Column(
            Modifier
                .weight(1f)
                .padding(16.dp),
        ) {
            hints.getOrNull(round)?.let { Text(it, fontSize = 18.sp, color = Color.Gray) }
            Spacer(Modifier.height(4.dp))
            Text("Poved ${round + 1} / ${sentences.size}", color = color, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            // Answer line: tap a word to send it back to the bank.
            val lineColor = when (checked) {
                true -> GameColors.Correct
                false -> GameColors.Wrong
                null -> color
            }
            FlowRow(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .border(2.dp, lineColor, RoundedCornerShape(14.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                placed.forEach { tile ->
                    WordChip(tile.text, lineColor, enabled = checked == null) { placed.remove(tile) }
                }
                if (placed.isNotEmpty() && ending.isNotEmpty()) {
                    Text(ending, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 10.dp))
                }
            }

            Spacer(Modifier.height(24.dp))

            // Word bank: tap a word to add it to the sentence.
            FlowRow(Modifier.fillMaxWidth()) {
                bank.filter { it !in placed }.forEach { tile ->
                    WordChip(tile.text, Color.LightGray, enabled = checked == null) { placed.add(tile) }
                }
            }
        }

        val isRight = checked
        if (isRight == null) {
            Button(
                onClick = {
                    val ok = placed.map { it.text } == words
                    if (ok) session.onCorrect() else session.onWrong()
                    checked = ok
                },
                enabled = placed.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = GameColors.Correct),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(52.dp),
            ) { Text("PREVERI", fontWeight = FontWeight.Bold) }
        } else {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(if (isRight) Color(0xFFD7FFB8) else Color(0xFFFFDFE0))
                    .padding(16.dp),
            ) {
                Text(
                    if (isRight) "Correct!" else "Not quite.",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isRight) Color(0xFF3B8A00) else Color(0xFFC62828),
                )
                Text(sentence, color = if (isRight) Color(0xFF3B8A00) else Color(0xFFC62828))
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = {
                        checked = null
                        if (!session.isOver) round++
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRight) GameColors.Correct else GameColors.Wrong,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) { Text("NAPREJ", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

/** One word in the bank. The id keeps two equal words (e.g. two "is") apart. */
private data class WordTile(val id: Int, val text: String)

/** "My sister is twelve years old." -> [My, sister, is, twelve, years, old] */
private fun splitWords(sentence: String): List<String> =
    sentence.trim().trimEnd('.', '?', '!').split(Regex("\\s+")).filter { it.isNotEmpty() }

@Composable
private fun WordChip(text: String, borderColor: Color, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .padding(4.dp)
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
private fun SentenceBuilderPreview() {
    MaterialTheme {
        Surface {
            SentenceBuilderGame(
                task = Task(
                    id = "ang_family_04_t8",
                    type = "game",
                    game = "sentence_builder",
                    prompt = "Build the sentence.",
                    answers = listOf("My sister is twelve years old.", "This is my brother.", "Her name is Ana."),
                    config = mapOf(
                        "hints" to "Moja sestra je stara 12 let.|To je moj brat.|Njeno ime je Ana.",
                        "extra" to "are,his,am",
                    ),
                ),
                onFinish = {},
            )
        }
    }
}
