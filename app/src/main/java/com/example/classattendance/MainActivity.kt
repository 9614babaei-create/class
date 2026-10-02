package com.example.classattendance

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classattendance.data.AppDatabase
import com.example.classattendance.data.Attendance
import com.example.classattendance.data.ExcelExport
import com.example.classattendance.data.SchoolClass
import com.example.classattendance.data.Settings
import com.example.classattendance.data.Student
import com.example.classattendance.data.Backup
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

        androidx.compose.runtime.CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {

            when {
                settings -> {
                    SettingsScreen(
                        vm = vm,
                        back = {
                            settings = false
                        }
                    )
                }

                selected == null -> {
                    Home(
                        classes = classes,
                        vm = vm,
                        open = {
                            selected = it
                        },
                        settings = {
                            settings = true
                        }
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

        LazyColumn(

            modifier = Modifier
                .fillMaxSize()
                .padding(padding),

            contentPadding = PaddingValues(16.dp),

            verticalArrangement = Arrangement.spacedBy(10.dp)

        ) {

            items(
                items = classes,
                key = {
                    it.id
                }
            ) { schoolClass ->

                Card(
                    onClick = {
                        open(schoolClass)
                    },

                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row(

                        modifier = Modifier.padding(16.dp),

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
                            contentDescription = "خروجی Excel"
                        )
                    }

                    IconButton(
                        onClick = {
                            vm.backup()
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.Backup,
                            contentDescription = "پشتیبان گیری"
                        )
                    }

                    IconButton(
                        onClick = {
                            addStudent = true
                        }
                    ) {

                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "افزودن دانش آموز"
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

                AttendanceScreen(
                    students = students,
                    attendance = attendance,
                    date = date,
                    onPrevious = {
                        date = date.minusDays(1)
                    },
                    onNext = {
                        date = date.plusDays(1)
                    },
                    vm = vm
                )

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
fun AttendanceScreen(
    students: List<Student>,
    attendance: List<Attendance>,
    date: LocalDate,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    vm: VM
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = onPrevious
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
                onClick = onNext
            ) {

                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "روز بعد"
                )
            }
        }

        LazyColumn(

            modifier = Modifier.fillMaxSize(),

            contentPadding = PaddingValues(12.dp),

            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            items(
                items = students,
                key = {
                    it.id
                }
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
                                    contentDescription = "حذف دانش آموز"
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(
            items = students,
            key = {
                it.id
            }
        ) { student ->

            val list by vm
                .studentAttendance(student.id)
                .collectAsState(initial = emptyList())

            val monthPrefix =
                date.toString().substring(0, 7)

            val monthList =
                list.filter {
                    it.date.startsWith(monthPrefix)
                }

            val present =
                monthList.count {
                    it.status == "حاضر"
                }

            val absent =
                monthList.count {
                    it.status == "غایب"
                }

            val late =
                monthList.count {
                    it.status == "تأخیر"
                }

            val justified =
                monthList.count {
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

                    Text(
                        text = "حاضر: $present   غایب: $absent   تأخیر: $late   موجه: $justified"
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

    val classes =
        db.classDao().observeAll()

    val settings =
        Settings(app)

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

    fun addClass(name: String) {

        io {
            db.classDao().insert(
                SchoolClass(
                    name = name
                )
            )
        }
    }

    fun deleteClass(
        schoolClass: SchoolClass
    ) {

        io {
            db.classDao().delete(
                schoolClass
            )
        }
    }

    fun addStudent(
        classId: Long,
        name: String
    ) {

        io {
            db.studentDao().insert(
                Student(
                    classId = classId,
                    name = name
                )
            )
        }
    }

    fun deleteStudent(
        student: Student
    ) {

        io {

            db.attendanceDao()
                .deleteForStudent(student.id)

            db.studentDao()
                .delete(student)
        }
    }

    fun setAttendance(
        studentId: Long,
        date: String,
        status: String
    ) {

        io {

            db.attendanceDao().upsert(
                Attendance(
                    studentId = studentId,
                    date = date,
                    status = status
                )
            )
        }
    }

    fun exportExcel(
        schoolClass: SchoolClass,
        students: List<Student>
    ) {

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
    }

    fun backup() {

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
}
