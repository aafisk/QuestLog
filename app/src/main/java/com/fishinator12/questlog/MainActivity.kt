package com.fishinator12.questlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.fishinator12.questlog.ui.theme.QuestLogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QuestLogTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Dashboard()
                }
            }
        }
    }
}

class Quest(var name: String = "New Quest", var category: Int = 1,
            var weight: Int = 1, var deadline: String = "",
            var notes: String = "") {
}

@Composable
fun Dashboard( modifier: Modifier = Modifier) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column() {
            Row(modifier = modifier.padding(100.dp)) {
                // TODO Add character/monster images and landscape
            }
            Row(modifier = modifier.padding(50.dp)) {
                // TODO Add character stats and monster requirements
            }
            QuestList()
        }
        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(onClick = { },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(30.dp)
            ) {
                // TODO add icon here
            }
        }
    }
}

@Preview
@Composable
fun DashboardPreview() {
    Dashboard()
}

@Composable
fun Quest( quest: Quest, modifier: Modifier = Modifier ) {
    Surface(color = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(15.dp))
            .border(0.5.dp, MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(15.dp))
    ) {
        Row( modifier = modifier
            .fillMaxWidth()
            .heightIn(0.dp, 75.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = modifier.padding(start = 16.dp),
            ) {
                Text("Sep. 3")
                Text("12:00 PM")
            }
            Text(quest.name, modifier = modifier
                .weight(3.75f)
                .padding(start = 12.dp))
            Surface(color = MaterialTheme.colorScheme.secondary, modifier = modifier.weight(0.8f)) {
                StarImage(quest.weight)
            }
        }
    }
}

@Preview
@Composable
fun QuestPreview() {
    QuestLogTheme {
        Quest("A task that you should really do already")
    }
}

@Composable
fun StarImage( num: Int, modifier: Modifier = Modifier) {
    val imageName = "star$num"
    val context = LocalContext.current

    val imageId = remember {
        context.resources.getIdentifier(imageName, "drawable", context.packageName)
    }

    Image( painter = painterResource(id = imageId),
        contentDescription = "stars",
        modifier = modifier.fillMaxHeight())
}

@Composable
fun QuestList( modifier: Modifier = Modifier,
               quests: List<Quest> = List(20) { Quest() }
) {
    LazyColumn(modifier = modifier.padding(vertical = 4.dp, horizontal = 4.dp)) {
        items(items = quests) { quest ->
            Quest(quest = quest)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuestListPreview() {
    QuestLogTheme {
        QuestList()
    }
}

@Composable
fun NewQuest( modifier: Modifier = Modifier,
              quest: Quest = Quest()
) {
    val context = LocalContext.current
    var questName by remember { mutableStateOf(quest.name) }
    var sliderPosition by remember { mutableIntStateOf(quest.weight) }
    var selectedCategory by remember { mutableIntStateOf(quest.category - 1) }
    var questNotes by remember { mutableStateOf(quest.notes) }
    var categories = remember {
        (1..4).mapNotNull { i ->
            val colorName = "category$i"
            val colorId = context.resources.getIdentifier(colorName, "color", context.packageName)
            if (colorId != 0) {
                Color(context.resources.getColor(colorId, context.theme))
            } else {
                null
            }
        }
    }

    Surface() {
        Column(verticalArrangement = Arrangement.Center) {
            Text(text = quest.name)
            TextField(value = "", onValueChange = { questName = it },
                placeholder = { Text("Quest Name...") })
            Row() {
                Surface(color = categories[selectedCategory],
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                ) {
                    StarImage(quest.weight)
                }
                Slider( value = sliderPosition.toFloat(),
                    onValueChange = { sliderPosition = it.toInt() },
                    valueRange = 1f..7f,
                    steps = 5
                )
            }
            Row() {
                categories.forEachIndexed { i, color ->
                    Box( modifier = modifier
                        .size(50.dp)
                        .background(color)
                        .clickable {
                            selectedCategory = i
                        }
                    )
                }
            }
            TextField(value = questNotes, onValueChange = { questNotes = it },
                placeholder = { Text("Notes...") })
        }
    }
}

@Composable
@Preview()
fun NewQuestPreview() {
    QuestLogTheme {
        NewQuest()
    }
}


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val extraPadding by animateDpAsState(
        if (expanded) 48.dp else 1.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = "extraPadding"
    )

    Surface(color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Row(modifier = Modifier.padding(24.dp)) {
            Column(modifier = Modifier
                .weight(1f)
                .padding(bottom = extraPadding)) {
                Text("Hello ")
                Text("$name!")
            }
            ElevatedButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Show less" else "Show more")
            }
        }
    }
}

@Composable
fun MyApp( modifier: Modifier = Modifier ) {
    var shouldShowOnboarding by rememberSaveable { mutableStateOf(true) }

    Surface(modifier) {
        if (shouldShowOnboarding) {
            OnboardingScreen(onContinueClicked = {shouldShowOnboarding = false})
        } else {
            Greetings()
        }
    }
}

@Preview
@Composable
fun MyAppPreview() {
    QuestLogTheme {
        MyApp(Modifier.fillMaxSize())
    }
}

@Composable
fun OnboardingScreen(modifier: Modifier = Modifier,
                     onContinueClicked: () -> Unit
) {
    var shouldShowOnboarding = remember { mutableStateOf(true) }

    Column( modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to the Basics Codelab!")
        Button(
            modifier = Modifier.padding(vertical = 24.dp),
            onClick = onContinueClicked
        ) {
            Text("Continue")
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardingPreview() {
    QuestLogTheme {
        OnboardingScreen(onContinueClicked = {})
    }
}


@Composable
fun Greetings(
    modifier: Modifier = Modifier,
    names: List<String> = List(1000) { "$it" }
) {
    LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
        items(items = names) { name ->
            Greeting(name = name)
        }
    }
}

@Preview(showBackground = true, widthDp = 320)
@Composable
fun GreetingsPreview() {
    QuestLogTheme {
        Greetings()
    }
}


