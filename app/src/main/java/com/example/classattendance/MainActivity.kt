package com.example.classattendance

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classattendance.data.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Composable
fun App(vm: VM = viewModel()) {

    val classes by vm.classes.collectAsState(initial = emptyList())

    var selected by remember {
        mutableStateOf<SchoolClass?>(null)
    }

    var settings by remember {
        mutableStateOf(false)
    }

    MaterialTheme {

        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {

            when {
                settings -> {
                    SettingsScreen(vm) {
                        settings = false
                    }
                }

                selected == null -> {
                    Home(
                        classes = classes,
                        vm = vm,
                        open = { selected = it },
                        settings = { settings = true }
                    )
                }

                else -> {
                    ClassPage(
                        c = selected!!,
                        vm = vm,
                        back = {
                            selected = null
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(
    classes: List<SchoolClass>,
    vm: VM,
    open: (SchoolClass) -> Unit,
    settings: () -> Unit
) {

    var add by remember {
        mutableStateOf(false)
    }

    var name by remember {
        mutableStateOf("")
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("حضور و غیاب کلاسی")
                },

                actions = {

                    IconButton(
                        onClick = settings
                    ) {

                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "تنظیمات"
                        )
                    }
                }
            )
        },

        floatingActionButton = {

            FloatingActionButton(
                onClick = {
                    add = true
                }
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "افزودن کلاس"
                )
            }
        }

    ) { padding ->

        if (classes.isEmpty()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "هنوز کلاسی اضافه نشده است.\nبرای افزودن کلاس روی + بزنید.",
                    textAlign = TextAlign.Center
                )
            }

        } else {

            LazyColumn(

                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),

                contentPadding = PaddingValues(16.dp),

                verticalArrangement = Arrangement.spacedBy(10.dp)

            ) {

                items(
                    items = classes,
                    key = { it.id }
                ) { schoolClass ->

                    Card(
                        onClick = {
                            open(schoolClass)
                        },

                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Row(

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),

                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null
                            )

                            Spacer(
                                modifier = Modifier.width(12.dp)
                            )

                            Text(
                                text = schoolClass.name,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = {
                                    vm.deleteClass(schoolClass)
                                }
                            ) {

                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "حذف کلاس"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (add) {

        DialogSimple(

            title = "کلاس جدید",

            label = "نام کلاس",

            value = name,

            onValue = {
                name = it
            },

            ok = {

                if (name.isNotBlank()) {
                    vm.addClass(name.trim())
                }

                name = ""
                add = false
            },

            cancel = {
                name = ""
                add = false
            }
        )
    }
}

@Composable
fun DialogSimple(
    title: String,
    label: String,
    value: String,
    onValue: (String) -> Unit,
    ok: () -> Unit,
    cancel: () -> Unit
) {

    AlertDialog(

        onDismissRequest = cancel,

        title = {
            Text(title)
        },

        text = {

            OutlinedTextField(

                value = value,

                onValueChange = onValue,

                label = {
                    Text(label)
                },

                singleLine = true,

                modifier = Modifier.fillMaxWidth()
            )
        },

        confirmButton = {

            TextButton(
                onClick = ok
            ) {
                Text("تأیید")
            }
        },

        dismissButton = {

            TextButton(
                onClick = cancel
            ) {
                Text("انصراف")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassPage(
    c: SchoolClass,
    vm: VM,
    back: () -> Unit
) {

    val students by vm
        .students(c.id)
        .collectAsState(initial = emptyList())

    var date by remember {
        mutableStateOf(LocalDate.now())
    }

    var tab by remember {
        mutableIntStateOf(0)
    }

    val attendance by vm
        .attendance(date.toString())
        .collectAsState(initial = emptyList())

    var addStudent by remember {
        mutableStateOf(false)
    }

    var studentName by remember {
        mutableStateOf("")
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text(c.name)
                },

                navigationIcon = {

                    IconButton(
                        onClick = back
                    ) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                },

                actions = {

                    IconButton(
                        onClick = {
                            vm.exportExcel(c, students)
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.TableView,
                            contentDescription = "Excel"
                        )
                    }

                    IconButton(
                        onClick = {
                            vm.backup()
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = "پشتیبان"
                        )
                    }

                    IconButton(
                        onClick = {
                            addStudent = true
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "افزودن دانش‌آموز"
                        )
                    }
                }
            )
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            TabRow(
                selectedTabIndex = tab
            ) {

                Tab(
                    selected = tab == 0,

                    onClick = {
                        tab = 0
                    },

                    text = {
                        Text("حضور و غیاب")
                    }
                )

                Tab(
                    selected = tab == 1,

                    onClick = {
                        tab = 1
                    },

                    text = {
                        Text("گزارش")
                    }
                )
            }

            if (tab == 0) {

                Row(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),

                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = {
                            date = date.minusDays(1)
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "روز قبل"
                        )
                    }

                    val jalali = Jalali.toJalali(
                        date.year,
                        date.monthValue,
                        date.dayOfMonth
                    )

                    Text(

                        text = "شمسی: ${jalali.first}/${
                            "%02d".format(jalali.second)
                        }/${
                            "%02d".format(jalali.third)
                        }",

                        modifier = Modifier.weight(1f),

                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = {
                            date = date.plusDays(1)
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "روز بعد"
                        )
                    }
                }

                if (students.isEmpty()) {

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = "هنوز دانش‌آموزی اضافه نشده است."
                        )
                    }

                } else {

                    LazyColumn(

                        modifier = Modifier.fillMaxSize(),

                        contentPadding = PaddingValues(12.dp),

                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        items(
                            items = students,
                            key = { it.id }
                        ) { student ->

                            val status =
                                attendance
                                    .firstOrNull {
                                        it.studentId == student.id
                                    }
                                    ?.status
                                    ?: "ثبت نشده"

                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Column(
                                    modifier = Modifier.padding(12.dp)
                                ) {

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {

                                        Text(
                                            text = student.name,
                                            modifier = Modifier.weight(1f)
                                        )

                                        IconButton(
                                            onClick = {
                                                vm.deleteStudent(student)
                                            }
                                        ) {

                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "حذف دانش‌آموز"
                                            )
                                        }
                                    }

                                    Spacer(
                                        modifier = Modifier.height(6.dp)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement =
                                            Arrangement.spacedBy(4.dp)
                                    ) {

                                        listOf(
                                            "حاضر",
                                            "غایب",
                                            "تأخیر",
                                            "موجه"
                                        ).forEach { item ->

                                            FilterChip(

                                                selected = status == item,

                                                onClick = {
                                                    vm.setAttendance(
                                                        student.id,
                                                        date.toString(),
                                                        item
                                                    )
                                                },

                                                label = {
                                                    Text(item)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            } else {

                Report(
                    students = students,
                    vm = vm,
                    date = date
                )
            }
        }
    }

    if (addStudent) {

        DialogSimple(

            title = "دانش‌آموز جدید",

            label = "نام و نام خانوادگی",

            value = studentName,

            onValue = {
                studentName = it
            },

            ok = {

                if (studentName.isNotBlank()) {

                    vm.addStudent(
                        c.id,
                        studentName.trim()
                    )
                }

                studentName = ""
                addStudent = false
            },

            cancel = {

                studentName = ""
                addStudent = false
            }
        )
    }
}

@Composable
fun Report(
    students: List<Student>,
    vm: VM,
    date: LocalDate
) {

    LazyColumn(

        modifier = Modifier.fillMaxSize(),

        contentPadding = PaddingValues(12.dp),

        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        item {

            val jalali = Jalali.toJalali(
                date.year,
                date.monthValue,
                date.dayOfMonth
            )

            Text(
                text = "گزارش ماه ${jalali.second}",
                style = MaterialTheme.typography.titleLarge
            )
        }

        items(
            items = students,
            key = { it.id }
        ) { student ->

            val list by vm
                .studentAttendance(student.id)
                .collectAsState(initial = emptyList())

            val prefix = date
                .toString()
                .substring(0, 7)

            val monthList = list.filter {
                it.date.startsWith(prefix)
            }

            val present = monthList.count {
                it.status == "حاضر"
            }

            val absent = monthList.count {
                it.status == "غایب"
            }

            val late = monthList.count {
                it.status == "تأخیر"
            }

            val justified = monthList.count {
                it.status == "موجه"
            }

            val percentage =
                if (monthList.isEmpty()) {
                    0
                } else {
                    present * 100 / monthList.size
                }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    modifier = Modifier.padding(14.dp)
                ) {

                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )

                    Text(
                        text = "حاضر: $present   غایب: $absent   تأخیر: $late   موجه: $justified"
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = "درصد حضور: $percentage%"
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: VM,
    back: () -> Unit
) {

    var school by remember {
        mutableStateOf(vm.settings.schoolName)
    }

    var teacher by remember {
        mutableStateOf(vm.settings.teacherName)
    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {
                    Text("تنظیمات")
                },

                navigationIcon = {

                    IconButton(
                        onClick = back
                    ) {

                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "بازگشت"
                        )
                    }
                }
            )
        }

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(

                value = school,

                onValueChange = {
                    school = it
                },

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("نام مدرسه")
                },

                singleLine = true
            )

            OutlinedTextField(

                value = teacher,

                onValueChange = {
                    teacher = it
                },

                modifier = Modifier.fillMaxWidth(),

                label = {
                    Text("نام معلم")
                },

                singleLine = true
            )

            Button(

                onClick = {

                    vm.settings.schoolName = school
                    vm.settings.teacherName = teacher

                    back()
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text("ذخیره تنظیمات")
            }

            Text(
                text = "برنامه کاملاً آفلاین است و اطلاعات روی دستگاه ذخیره می‌شود.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

class VM(
    app: Application
) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)

    val classes = db.classDao().observeAll()

    val settings = Settings(app)

    fun students(id: Long) =
        db.studentDao().observeByClass(id)

    fun attendance(date: String) =
        db.attendanceDao().observeByDate(date)

    fun studentAttendance(id: Long) =
        db.attendanceDao().observeByStudent(id)

    private fun io(
        block: suspend () -> Unit
    ) {
        CoroutineScope(
            Dispatchers.IO
        ).launch {
            block()
        }
    }

    fun addClass(name: String) =
        io {
            db.classDao().insert(
                SchoolClass(
                    name = name
                )
            )
        }

    fun deleteClass(
        schoolClass: SchoolClass
    ) =
        io {
            db.classDao().delete(schoolClass)
        }

    fun addStudent(
        classId: Long,
        name: String
    ) =
        io {
            db.studentDao().insert(
                Student(
                    classId = classId,
                    name = name
                )
            )
        }

    fun deleteStudent(
        student: Student
    ) =
        io {

            db.attendanceDao()
                .deleteForStudent(student.id)

            db.studentDao()
                .delete(student)
        }

    fun setAttendance(
        studentId: Long,
        date: String,
        status: String
    ) =
        io {

            db.attendanceDao().upsert(
                Attendance(
                    studentId = studentId,
                    date = date,
                    status = status
                )
            )
        }

    fun exportExcel(
        schoolClass: SchoolClass,
        students: List<Student>
    ) =
        io {

            val intent = ExcelExport.create(
                getApplication(),
                schoolClass,
                students,
                db,
                settings.schoolName,
                settings.teacherName
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            getApplication<Application>()
                .startActivity(
                    Intent.createChooser(
                        intent,
                        "ارسال فایل Excel"
                    )
                )
        }

    fun backup() =
        io {

            val intent = Backup.create(
                getApplication(),
                db
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            getApplication<Application>()
                .startActivity(
                    Intent.createChooser(
                        intent,
                        "پشتیبان‌گیری"
                    )
                )
        }
}
