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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                    MyApp()
                }
            }
        }
    }
}

class Quest(var name: String, var category: Int,
            var weight: Int, var deadline: String,
            var notes: String) {
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
            FloatingActionButton(onClick = {},
                modifier = Modifier.align(Alignment.BottomEnd).padding(30.dp)
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
fun Quest( questName: String, modifier: Modifier = Modifier ) {
    Surface(color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 3.dp).clip(RoundedCornerShape(15.dp)).border(0.5.dp, MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(15.dp))
    ) {
        Row( modifier = modifier.fillMaxWidth().heightIn(0.dp, 75.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = modifier.padding(start = 16.dp),
            ) {
                Text("Sep. 3")
                Text("12:00 PM")
            }
            Text(questName, modifier = modifier.weight(3.75f).padding(start = 12.dp))
            Surface(color = MaterialTheme.colorScheme.secondary, modifier = modifier.weight(0.8f)) {
                Image(
                    painter = painterResource(R.drawable.star2), // TODO variable number???
                    contentDescription = "stars",
                    modifier = modifier.fillMaxHeight()
                )
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
fun QuestList( modifier: Modifier = Modifier,
               quests: List<String> = List(20) { "This is quest #$it And a bunc oi other text" }
) {
    LazyColumn(modifier = modifier.padding(vertical = 4.dp, horizontal = 4.dp)) {
        items(items = quests) { quest ->
            Quest(questName = quest)
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


