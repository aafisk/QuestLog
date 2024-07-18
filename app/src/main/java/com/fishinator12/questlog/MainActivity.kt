package com.fishinator12.questlog

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.materialIcons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeCompilerApi
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.fishinator12.questlog.ui.theme.QuestLogTheme
import kotlinx.coroutines.delay
import kotlin.random.Random
import com.google.gson.Gson

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
                    QuestLog()
//                    Dashboard()
//                    NewQuest( quest = Quest() )
                }
            }
        }
    }
}

enum class EnemyTypes {
    GHOST,
    GOLEM
}

class Quest(var name: String = "", var category: Int = 1,
            var weight: Int = 1, var deadline: String = "",
            var notes: String = "") { }

class Character() {
    var level = 1
    var experience = 0

    fun addExperience(exp: Int) {
        if (experience + exp > 50) {
            level += 1
            experience = (experience + exp) % 50
        } else {
            experience += exp
        }
    }
}

class Enemy() {
    var type: EnemyTypes
    var requirements: MutableList<Int>
    var exp: Int = 5
    var alive: Boolean
    
    init {
        // Randomly select an enemy type
        type = EnemyTypes.entries.toTypedArray()[Random.nextInt(EnemyTypes.entries.size)]

        // Randomly assign requirements
        requirements = MutableList(4) { Random.nextInt(0, 8) }

        // Randomly set exp
        exp = Random.nextInt(5, 16)

        alive = true
    }

    fun updateRequirement(number: Int, index: Int) {
        requirements[index] = requirements[index] - number
        alive = !checkRequirements()
    }

    private fun checkRequirements(): Boolean {
        return requirements.all { it <= 0 }
    }

    fun revive() {
        requirements = MutableList(4) { Random.nextInt(0, 8) }
        exp = Random.nextInt(5, 16)
        alive = true
    }
}

@Composable
fun CatagoryColors(context: Context): List<Color> {
    return remember {
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
}

fun saveData(context: Context, character: Character, enemy: Enemy, questList: List<Quest>) {
    val sharedPreferences = context.getSharedPreferences("CharacterProgress", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()

    editor.putString("character", Gson().toJson(character))
    editor.putString("enemy", Gson().toJson(enemy))
    editor.putString("questList", Gson().toJson(questList))
    editor.apply()
}

fun loadData(context: Context): Triple<Character, Enemy, List<Quest>> {
    val sharedPreferences = context.getSharedPreferences("CharacterProgress", Context.MODE_PRIVATE)

    val characterJson = sharedPreferences.getString("character", null)
    val enemyJson = sharedPreferences.getString("enemy", null)
    val questListJson = sharedPreferences.getString("questList", null)

    val character = if (characterJson != null) Gson().fromJson(characterJson, Character::class.java) else Character()
    val enemy = if (enemyJson != null) Gson().fromJson(enemyJson, Enemy::class.java) else Enemy()
    val questList = if (questListJson != null) Gson().fromJson(questListJson, Array<Quest>::class.java).toList() else listOf()

    return Triple(character, enemy, questList)
}

@Composable
fun QuestLog( modifier: Modifier = Modifier ) {
    val navController = rememberNavController()
    var questList = remember { mutableStateListOf<Quest>() }
    var character by remember { mutableStateOf(Character()) }
    var enemy by remember { mutableStateOf(Enemy()) }
    var selectedQuestIndex = remember { mutableStateOf<Int?>(null) }
    val selectedQuest = remember { mutableStateOf<Quest?>(null) }

    // Update the selectedQuest state when questList or selectedQuestIndex changes
    LaunchedEffect(questList, selectedQuestIndex.value) {
        selectedQuest.value = if (questList.isNotEmpty() && selectedQuestIndex.value != null) questList[selectedQuestIndex.value!!] else null
    }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val (loadedCharacter, loadedEnemy, loadedQuestList) = loadData(context)
        character = loadedCharacter
        enemy = loadedEnemy
        questList.addAll(loadedQuestList)
    }

    NavHost(navController = navController, startDestination = "Dashboard") {
        composable("Dashboard") {
            Dashboard(navController = navController,
                quests = questList,
                character = character,
                enemy = enemy,
                selectedQuestIndex = selectedQuestIndex)
        }

        composable("NewQuest") {
            NewQuest(currentQuest = selectedQuest,
                selectedQuestIndex = selectedQuestIndex,
                navController = navController,
                quests = questList,
                character = character,
                enemy = enemy)
        }

        composable("ViewQuest") {
            if (selectedQuest != null) {
                ViewQuest(
                    navController = navController,
                    enemy = enemy,
                    character = character,
                    questList = questList,
                    questIndex = selectedQuestIndex)
            }
        }
    }
}

@Composable
@Preview
fun QuestLogPreview() {
    QuestLog()
}


@Composable
fun Dashboard( modifier: Modifier = Modifier, 
               navController: NavHostController,
               quests: MutableList<Quest>,
               character: Character,
               enemy: Enemy,
               selectedQuestIndex: MutableState<Int?>
) {
    //selectedQuestIndex.value = null
    Surface(color = colorResource(id =  R.color.tan)) {
        Box(modifier = modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = "background",
                modifier = modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                contentScale = ContentScale.FillWidth
            )
        }
        Column() {
            Spacer(modifier = modifier.height(150.dp))
            Row(modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Character
                AnimatedSprite(tilemapId = R.drawable.character_tilemap,
                    frames = 10,
                    frameLength = 100,
                    tileSize = 120,
                    imageSizeDP = 64,
                    modifier = modifier.padding(start = 40.dp))

                // Enemy
                AnimatedSprite(tilemapId = R.drawable.ghost_tilemap,
                    frames = 6,
                    frameLength = 100,
                    tileSize = 120,
                    imageSizeDP = 64,
                    flipSprite = true)
            }
//            Spacer(modifier = modifier.height(100.dp))
            Image(
                painter = painterResource(id = R.drawable.ground),
                contentDescription = "ground",
                modifier = modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Image() {}
                Column(modifier = Modifier.padding(horizontal = 10.dp)){
                    Text("Aaron",
                        modifier = modifier.padding(bottom = 6.dp),
                        style = TextStyle(fontSize = 22.sp))
                    Text("Level - ${character.level}", modifier = modifier.padding(bottom = 3.dp))
                    LinearProgressIndicator( progress = (character.experience.toFloat() / 50),
                        color = colorResource(id = R.color.category2),
                        trackColor = colorResource(id = R.color.greyFill),
                        modifier = modifier
                            .height(30.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .border(1.dp, Color.Black, RoundedCornerShape(5.dp))
                            .width(200.dp))
                }
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = modifier
                        .padding(bottom = 5.dp)
                        .height(50.dp)) {
                        if (enemy.requirements[0] > 0)
                            EnemyRequirement(requirement = enemy.requirements[0],
                                category = 0,
                                modifier = Modifier
                                    .padding(end = 5.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color.Black, RoundedCornerShape(10.dp)))
                        if (enemy.requirements[1] > 0)
                            EnemyRequirement(requirement = enemy.requirements[1],
                                category = 1,
                                modifier = Modifier
                                    .padding(end = 5.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color.Black, RoundedCornerShape(10.dp)))
                    }
                    Row(modifier = modifier.height(50.dp)) {
                        if (enemy.requirements[2] > 0)
                            EnemyRequirement(requirement = enemy.requirements[2],
                                category = 2,
                                modifier = Modifier
                                    .padding(end = 5.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color.Black, RoundedCornerShape(10.dp)))
                        if (enemy.requirements[3] > 0)
                            EnemyRequirement(requirement = enemy.requirements[3],
                                category = 3,
                                modifier = Modifier
                                    .padding(end = 5.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color.Black, RoundedCornerShape(10.dp)))
                    }
                }
            }
            QuestList(
                quests = quests,
                selectedQuest = selectedQuestIndex,
                navigate = { navController.navigate("ViewQuest") })
        }
        Box(modifier = Modifier.fillMaxSize()) {
            FloatingActionButton(
                onClick = { selectedQuestIndex.value = null
                    navController.navigate("NewQuest") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(30.dp),
                containerColor = colorResource(id = R.color.greyFill)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    }
}

@Preview
@Composable
fun DashboardPreview() {
    var showNewQuest = false
//    Dashboard(onNewClicked = {showNewQuest = true})
}

@Composable
fun Quest( quest: Quest, modifier: Modifier = Modifier ) {
    val context = LocalContext.current
    val categories = CatagoryColors(context)

    Surface(color = colorResource(id =  R.color.greyFill),
        modifier = modifier
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(15.dp))
            .border(1.dp, Color.Black, RoundedCornerShape(15.dp))
    ) {
        Row( modifier = modifier
            .fillMaxWidth()
            .heightIn(0.dp, 75.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
//            Column(modifier = modifier.padding(start = 16.dp),
//            ) {
//                Text("Sep. 3")
//                Text("12:00 PM")
//            }
            Text(quest.name, modifier = modifier
                .weight(3.75f)
                .padding(start = 12.dp))
            Surface(color = categories[quest.category], modifier = modifier.weight(0.8f)) {
                StarImage(quest.weight)
            }
        }
    }
}

@Preview
@Composable
fun QuestPreview() {
    QuestLogTheme {
        Quest( Quest() )
    }
}

@Composable
fun EnemyRequirement(requirement: Int, category: Int, modifier: Modifier = Modifier) {
    val categories = CatagoryColors(LocalContext.current)

    Surface( color = categories[category], modifier = modifier.width(75.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StarImage(1, modifier = Modifier.weight(2f))
            Text("$requirement",
                modifier = Modifier
                    .padding(end = 5.dp)
                    .weight(1f),
                style = TextStyle(fontSize = 24.sp))
        }
    }
}

@Preview
@Composable
fun EnemyRequirementPreview() {
    EnemyRequirement(requirement = 6, category = 2)
}

@Composable
fun AnimatedSprite(tilemapId: Int, frames: Int, frameLength: Int, tileSize: Int, imageSizeDP: Int,
                   flipSprite: Boolean = false, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val tileBitmap = remember {
        BitmapFactory.decodeResource(context.resources, tilemapId)
    }

    // Draw a black box if there is an error loading the bitmap
    if (tileBitmap == null) {
        Canvas(modifier = modifier.size(64.dp)) {
            drawIntoCanvas {
                it.nativeCanvas.drawColor(android.graphics.Color.BLACK)
            }
        }
        return
    }

    val tilemapImageBitmap = tileBitmap.asImageBitmap()
    var currentFrame by remember { mutableIntStateOf(0) }

    // Advance each frame
    LaunchedEffect(Unit) {
        while (true) {
            delay(frameLength.toLong())
            currentFrame = (currentFrame + 1) % frames
        }
    }

    Canvas(modifier = modifier.size(imageSizeDP.dp)) {
        //val row = currentFrame / (tileBitmap.width / tileSize)
        val col = currentFrame

        val srcLeft = col * tileSize
        val srcTop = 0
//        val srcRight = srcLeft + tileSize
//        val srcBottom = srcTop + tileSize

        drawContext.canvas.save()

        if (flipSprite) {
            // Flip horizontally
            drawContext.canvas.scale(-1f, 1f)
        }

        drawImage(
            image = tilemapImageBitmap,
            srcOffset = IntOffset(srcLeft, srcTop),
            srcSize = IntSize(tileSize, tileSize),
            dstSize = IntSize(tileSize, tileSize),
        )

        drawContext.canvas.restore()
    }
}

@Preview
@Composable
fun AnimatedSpritePreview() {
    AnimatedSprite(tilemapId = R.drawable.soldier_tilemap,
        frames = 8,
        frameLength = 100,
        tileSize = 100,
        imageSizeDP = 100,
        modifier = Modifier.padding(start = 40.dp)
    )
}


@Composable
fun StarImage( num: Int, modifier: Modifier = Modifier ) {
    var imageName = "star$num"
    val context = LocalContext.current

    var imageId = remember(num) {
        context.resources.getIdentifier(imageName, "drawable", context.packageName)
    }

    Image( painter = painterResource(id = imageId),
        contentDescription = "stars",
        modifier = modifier.fillMaxHeight())
}

@Composable
fun QuestList(modifier: Modifier = Modifier,
              quests: List<Quest> = List(0) { Quest() },
              selectedQuest: MutableState<Int?>,
              navigate: (String) -> Unit
) {
    // Display each quest to the user
    if (quests.isNotEmpty()) {
        LazyColumn(modifier = modifier.padding(vertical = 4.dp, horizontal = 4.dp)) {
            itemsIndexed(items = quests) { index, quest ->
                Box(modifier = Modifier.clickable {
                    selectedQuest.value = index
                    navigate("ViewQuest")
                }) {
                    Quest(quest = quest)
                }
            }
        }
    } else {
        // Display a message if there are no quests to display
        Box(modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("You have no quests right now",
                style = TextStyle(fontSize = 24.sp))
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun QuestListPreview() {
//    QuestLogTheme {
//        QuestList()
//    }
//}

@Composable
fun NewQuest( modifier: Modifier = Modifier,
              currentQuest: MutableState<Quest?>,
              selectedQuestIndex: MutableState<Int?>,
              navController: NavHostController,
              quests: MutableList<Quest>,
              character: Character,
              enemy: Enemy
) {
    var quest = currentQuest.value ?: Quest()
    val newQuest = quest.name == "" && quest.weight == 1
    val context = LocalContext.current
    var questName by remember { mutableStateOf(quest.name) }
    var weight by remember { mutableIntStateOf(quest.weight) }
    var selectedCategory by remember { mutableIntStateOf(if (newQuest) quest.category - 1 else quest.category) }
    var questNotes by remember { mutableStateOf(quest.notes) }
    val categories = CatagoryColors(context)

    Surface(color = colorResource(id = R.color.tan)) {
        Column(modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Text
            Text(text = if (quest.name != "") "Edit Quest" else "New Quest",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 24.sp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth())

            // Quest name text field
            TextField(value = questName, onValueChange = {
                questName = it
                quest.name = it },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorResource(id = R.color.creamFill),
                    unfocusedContainerColor = colorResource(id = R.color.creamFill)
                ),
                placeholder = { Text("Quest Name...") },
                modifier = modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp)))

            // Slider Row
            Surface(color = colorResource(id = R.color.creamFill),
                modifier = modifier.clip(RoundedCornerShape(15.dp))
            ) {
                Row(modifier = modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = categories[selectedCategory],
                        modifier = Modifier
                            .size(75.dp)
                            .clip(CircleShape)
                    ) {
                        StarImage(quest.weight, modifier = modifier
                            .clip(CircleShape)
                            .border(1.dp, Color.Black, CircleShape))
                    }
//                    Text("$weight")
//                    Text(text = quest.weight.toString())
                    Slider(
                        value = weight.toFloat(),
                        onValueChange = {
                            weight = it.toInt()
                            quest.weight = it.toInt()
                        },
                        valueRange = 1f..7f,
                        steps = 6,
                        modifier = modifier.padding(start = 16.dp),
                    )
                }
            }

            // Category boxes
            Text("Category:")
            Row( horizontalArrangement = Arrangement.SpaceBetween,
                modifier = modifier.fillMaxWidth()
            ) {
                categories.forEachIndexed { i, color ->
                    Box( modifier = modifier
                        .size(height = 50.dp, width = 75.dp)
                        .background(color, RoundedCornerShape(20.dp))
                        .border(2.dp, Color.Black, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .clickable {
                            selectedCategory = i
                            quest.category = i
                        }
                    )
                }
            }

            // Notes text field
            TextField(value = questNotes,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = colorResource(id = R.color.creamFill),
                    unfocusedContainerColor = colorResource(id = R.color.creamFill)
                ),
                modifier = modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(10.dp)),
                onValueChange = {
                    questNotes = it
                    quest.notes = it},
                placeholder = { Text("Notes...") })

            // Return and Save buttons
            Spacer(modifier = modifier.weight(1f))
            Row( modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center, ) {
                LargeFloatingActionButton(onClick = {
                    navController.popBackStack()
                                                    },
                    modifier = modifier
                        .clip(CircleShape)
                        .border(2.dp, Color.Black, CircleShape),
                    containerColor = colorResource(id = R.color.greyFill)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                        modifier = modifier.scale(2f))
                }
                Spacer( modifier = modifier.width(45.dp))
                LargeFloatingActionButton(onClick = {
                    if (newQuest) {
                        quests.add(Quest(questName, selectedCategory, weight, notes = questNotes))
                    }
                    saveData(context, character, enemy, quests)
                    navController.navigate("Dashboard") },
                    modifier = modifier
                        .clip(CircleShape)
                        .border(2.dp, Color.Black, CircleShape),
                    containerColor = colorResource(id = R.color.greyFill)) {
                    Icon(Icons.Default.Save, contentDescription = "Save",
                        modifier = modifier.scale(2f))
                }
            }
        }
    }
}

fun checkQuestListSize(questList: MutableList<Quest>, questIndex: MutableState<Int?>): Boolean {
    return (questList.isNotEmpty() && questList.size > questIndex.value!!)
}

@Composable
fun ViewQuest(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    character: Character,
    enemy: Enemy,
    questList: MutableList<Quest>,
    questIndex: MutableState<Int?>
) {
    val context = LocalContext.current
    val categories = CatagoryColors(context)

    Surface(color = colorResource(id = R.color.tan)) {
        Column(modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Text
            Text(text = "Quest Info",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = TextStyle(fontSize = 24.sp),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth())

            // Display quest notes
            Column() {
                Text("Quest Name:", modifier = Modifier.padding(bottom = 3.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colorResource(id = R.color.creamFill))
                ) {
                    Text(
                        text = if (checkQuestListSize(questList, questIndex)) questList[questIndex.value!!].name else "",
                        modifier = modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                        style = TextStyle(fontSize = 18.sp)
                    )
                }
            }

            // Quest weight and category
            Surface(color = colorResource(id = R.color.creamFill),
                modifier = modifier
                    .clip(RoundedCornerShape(15.dp))
                    .fillMaxWidth()
            ) {
                Row(modifier = modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (checkQuestListSize(questList, questIndex)) categories[questList[questIndex.value!!].category] else categories[0],
                        modifier = Modifier
                            .size(75.dp)
                            .clip(CircleShape)
                    ) {
                        StarImage( if (checkQuestListSize(questList, questIndex)) questList[questIndex.value!!].weight else 1,
                            modifier = modifier
                            .clip(CircleShape)
                            .border(1.dp, Color.Black, CircleShape))
                    }
                    Column(verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(start = 16.dp)) {
                        Text(text = "Quest Weight: ${if (checkQuestListSize(questList, questIndex)) questList[questIndex.value!!].weight else 1}",
                            modifier = Modifier.padding(bottom = 10.dp),
                            fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Category: ",
                                fontWeight = FontWeight.Bold)
                            Box( modifier = modifier
                                .size(height = 25.dp, width = 50.dp)
                                .background(
                                    color = if (checkQuestListSize(questList, questIndex)) categories[questList[questIndex.value!!].category] else categories[0],
                                    RoundedCornerShape(20.dp)
                                )
                                .border(1.dp, Color.Black, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                            )
                        }
                    }
                }
            }

            // Display quest notes
            Column() {
                Text("Notes:", modifier = Modifier.padding(bottom = 3.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colorResource(id = R.color.creamFill))
                ) {
                    Text(
                        text = if (checkQuestListSize(questList, questIndex)) questList[questIndex.value!!].notes else "",
                        modifier = modifier
                            .padding(5.dp)
                            .fillMaxWidth()
                            .heightIn(min = 100.dp)
                    )
                }
            }

            // Return, Complete, and Save buttons
            Spacer(modifier = modifier.weight(1f))
            Row( modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom) {
                FloatingActionButton(onClick = { navController.popBackStack() },
                    modifier = modifier
                        .clip(CircleShape)
                        .border(2.dp, Color.Black, CircleShape),
                    containerColor = colorResource(id = R.color.greyFill)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                        modifier = modifier.scale(1.5f))
                }

                Spacer( modifier = modifier.width(30.dp) )
                LargeFloatingActionButton(onClick = {
                    enemy.updateRequirement(questList[questIndex.value!!].weight, questList[questIndex.value!!].category)

                    if (questIndex.value!! < 0 || questIndex.value!! >= questList.size) {
                        Log.e("Error", "Quest index out of bounds: ${questIndex.value}")
                    }
                    questList.removeAt(questIndex.value!!)

                    if (!enemy.alive){
                        character.addExperience(enemy.exp)
                        enemy.revive()
                    }
                    saveData(context, character, enemy, questList)
                    navController.navigate("Dashboard")

                },
                    modifier = modifier
                        .clip(CircleShape)
                        .border(2.dp, Color.Black, CircleShape),
                    containerColor = colorResource(id = R.color.category2)) {
                    Icon(Icons.Default.DoneOutline, contentDescription = "Complete",
                        modifier = modifier.scale(2f))
                }

                Spacer( modifier = modifier.width(30.dp))
                FloatingActionButton(onClick = {
                    navController.navigate("NewQuest")
                    },
                    modifier = modifier
                        .clip(CircleShape)
                        .border(2.dp, Color.Black, CircleShape),
                    containerColor = colorResource(id = R.color.greyFill)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit",
                        modifier = modifier.scale(1.5f))
                }
            }
        }
    }
}

//@Composable
//@Preview()
//fun NewQuestPreview() {
//    val navController = rememberNavController()
//    QuestLogTheme {
//        NewQuest(navController = navController)
//    }
//}


//@Composable
//fun Greeting(name: String, modifier: Modifier = Modifier) {
//    var expanded by rememberSaveable { mutableStateOf(false) }
//    val extraPadding by animateDpAsState(
//        if (expanded) 48.dp else 1.dp,
//        animationSpec = spring(
//            dampingRatio = Spring.DampingRatioMediumBouncy,
//            stiffness = Spring.StiffnessLow
//        ), label = "extraPadding"
//    )
//
//    Surface(color = MaterialTheme.colorScheme.primary,
//        modifier = modifier.padding(vertical = 4.dp, horizontal = 8.dp)
//    ) {
//        Row(modifier = Modifier.padding(24.dp)) {
//            Column(modifier = Modifier
//                .weight(1f)
//                .padding(bottom = extraPadding)) {
//                Text("Hello ")
//                Text("$name!")
//            }
//            ElevatedButton(onClick = { expanded = !expanded }) {
//                Text(if (expanded) "Show less" else "Show more")
//            }
//        }
//    }
//}
//
//@Composable
//fun MyApp( modifier: Modifier = Modifier ) {
//    var shouldShowOnboarding by rememberSaveable { mutableStateOf(true) }
//
//    Surface(modifier) {
//        if (shouldShowOnboarding) {
//            OnboardingScreen(onContinueClicked = {shouldShowOnboarding = false})
//        } else {
//            Greetings()
//        }
//    }
//}
//
//@Preview
//@Composable
//fun MyAppPreview() {
//    QuestLogTheme {
//        MyApp(Modifier.fillMaxSize())
//    }
//}
//
//@Composable
//fun OnboardingScreen(modifier: Modifier = Modifier,
//                     onContinueClicked: () -> Unit
//) {
//    var shouldShowOnboarding = remember { mutableStateOf(true) }
//
//    Column( modifier = modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text("Welcome to the Basics Codelab!")
//        Button(
//            modifier = Modifier.padding(vertical = 24.dp),
//            onClick = onContinueClicked
//        ) {
//            Text("Continue")
//        }
//    }
//}
//
//@Preview(showBackground = true, widthDp = 320, heightDp = 320)
//@Composable
//fun OnboardingPreview() {
//    QuestLogTheme {
//        OnboardingScreen(onContinueClicked = {})
//    }
//}
//
//
//@Composable
//fun Greetings(
//    modifier: Modifier = Modifier,
//    names: List<String> = List(1000) { "$it" }
//) {
//    LazyColumn(modifier = modifier.padding(vertical = 4.dp)) {
//        items(items = names) { name ->
//            Greeting(name = name)
//        }
//    }
//}
//
//@Preview(showBackground = true, widthDp = 320)
//@Composable
//fun GreetingsPreview() {
//    QuestLogTheme {
//        Greetings()
//    }
//}


