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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classattendance.data.*
import kotlinx.coroutines.*
import java.time.LocalDate

class MainActivity: ComponentActivity(){
    override fun onCreate(b:Bundle?){super.onCreate(b);setContent{App()}}
}

@Composable
fun App(vm:VM=viewModel()){
    val classes by vm.classes.collectAsState(initial=emptyList())
    var selected by remember{mutableStateOf<SchoolClass?>(null)}
    var settings by remember{mutableStateOf(false)}
    MaterialTheme{CompositionLocalProvider(LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl){
        if(settings) SettingsScreen(vm){settings=false}
        else if(selected==null) Home(classes,vm,{selected=it},{settings=true})
        else ClassPage(selected!!,vm){selected=null}
    }}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun Home(cs:List<SchoolClass>,vm:VM,open:(SchoolClass)->Unit,settings:()->Unit){
    var add by remember{mutableStateOf(false)}; var n by remember{mutableStateOf("")}
    Scaffold(topBar={TopAppBar({Text("حضور و غیاب کلاسی — نسخه ۳")},actions={
        IconButton(settings){Icon(Icons.Default.Settings,"تنظیمات")}
    })},floatingActionButton={FloatingActionButton({add=true}){Icon(Icons.Default.Add,"افزودن")}}){p->
        LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            items(cs,key={it.id}){c->Card({open(c)},Modifier.fillMaxWidth()){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){
                Icon(Icons.Default.School,null);Spacer(Modifier.width(12.dp));Text(c.name,Modifier.weight(1f));IconButton({vm.deleteClass(c)}){Icon(Icons.Default.Delete,"حذف")}
            }}}
        }
    }
    if(add) DialogSimple("کلاس جدید","نام کلاس",n,{n=it},{if(n.isNotBlank())vm.addClass(n);n="";add=false},{add=false})
}

@Composable fun DialogSimple(title:String,label:String,value:String,onValue:(String)->Unit,ok:()->Unit,cancel:()->Unit){
    AlertDialog(onDismissRequest=cancel,title={Text(title)},text={OutlinedTextField(value,onValue,label={Text(label)},singleLine=true)},
        confirmButton={TextButton(ok){Text("تأیید")}},dismissButton={TextButton(cancel){Text("انصراف")}})
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ClassPage(c:SchoolClass,vm:VM,back:()->Unit){
    val students by vm.students(c.id).collectAsState(initial=emptyList())
    var date by remember{mutableStateOf(LocalDate.now())}; var tab by remember{mutableIntStateOf(0)}
    val at by vm.attendance(date.toString()).collectAsState(initial=emptyList())
    var add by remember{mutableStateOf(false)};var n by remember{mutableStateOf("")}
    Scaffold(topBar={TopAppBar({Text(c.name)},navigationIcon={IconButton(back){Icon(Icons.Default.ArrowBack,"بازگشت")}},
        actions={
            IconButton({vm.exportExcel(c,students)}){Icon(Icons.Default.TableView,"Excel")}
            IconButton({vm.backup()}){Icon(Icons.Default.Backup,"پشتیبان")}
            IconButton({add=true}){Icon(Icons.Default.PersonAdd,"دانش‌آموز")}
        })}){p->
        Column(Modifier.fillMaxSize().padding(p)){
            TabRow(tab){Tab(tab==0,{tab=0},{Text("حضور و غیاب")});Tab(tab==1,{tab=1},{Text("گزارش")})}
            if(tab==0){
                Row(Modifier.fillMaxWidth().padding(10.dp),verticalAlignment=Alignment.CenterVertically){
                    IconButton({date=date.minusDays(1)}){Icon(Icons.Default.ChevronRight,"قبل")}
                    val j=Jalali.toJalali(date.year,date.monthValue,date.dayOfMonth)
                    Text("شمسی: ${j.first}/${"%02d".format(j.second)}/${"%02d".format(j.third)}",Modifier.weight(1f),textAlign=TextAlign.Center)
                    IconButton({date=date.plusDays(1)}){Icon(Icons.Default.ChevronLeft,"بعد")}
                }
                LazyColumn(contentPadding=PaddingValues(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                    items(students,key={it.id}){s->
                        val status=at.firstOrNull{it.studentId==s.id}?.status?:"ثبت نشده"
                        Card(Modifier.fillMaxWidth()){Column(Modifier.padding(12.dp)){
                            Row(verticalAlignment=Alignment.CenterVertically){Text(s.name,Modifier.weight(1f));IconButton({vm.deleteStudent(s)}){Icon(Icons.Default.Delete,"حذف")}}
                            Row(horizontalArrangement=Arrangement.spacedBy(4.dp)){listOf("حاضر","غایب","تأخیر","موجه").forEach{x->FilterChip(status==x,{vm.setAttendance(s.id,date.toString(),x)},label={Text(x)})}}
                        }}
                    }
                }
            }else Report(students,vm,date)
        }
    }
    if(add) DialogSimple("دانش‌آموز جدید","نام و نام خانوادگی",n,{n=it},{if(n.isNotBlank())vm.addStudent(c.id,n);n="";add=false},{add=false})
}

@Composable fun Report(students:List<Student>,vm:VM,date:LocalDate){
    LazyColumn(contentPadding=PaddingValues(12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
        item{Text("گزارش ماه ${Jalali.toJalali(date.year,date.monthValue,date.dayOfMonth).second}",style=MaterialTheme.typography.titleLarge)}
        items(students,key={it.id}){s->
            val list by vm.studentAttendance(s.id).collectAsState(initial=emptyList())
            val m=list.filter{it.date.startsWith(date.toString().substring(0,7))}
            val p=m.count{it.status=="حاضر"};val a=m.count{it.status=="غایب"};val l=m.count{it.status=="تأخیر"};val e=m.count{it.status=="موجه"}
            Card(Modifier.fillMaxWidth()){Column(Modifier.padding(14.dp)){Text(s.name,style=MaterialTheme.typography.titleMedium);Text("حاضر: $p   غایب: $a   تأخیر: $l   موجه: $e");Text("درصد حضور: ${if(m.isEmpty())0 else p*100/m.size}%")}}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun SettingsScreen(vm:VM,back:()->Unit){
    var school by remember{mutableStateOf(vm.settings.schoolName)}
    var teacher by remember{mutableStateOf(vm.settings.teacherName)}
    Scaffold(topBar={TopAppBar({Text("تنظیمات")},navigationIcon={IconButton(back){Icon(Icons.Default.ArrowBack,"بازگشت")}})}){p->
        Column(Modifier.padding(p).padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
            OutlinedTextField(school,{school=it},Modifier.fillMaxWidth(),label={Text("نام مدرسه")})
            OutlinedTextField(teacher,{teacher=it},Modifier.fillMaxWidth(),label={Text("نام معلم")})
            Button({vm.settings.schoolName=school;vm.settings.teacherName=teacher;back()},Modifier.fillMaxWidth()){Text("ذخیره تنظیمات")}
            Text("برنامه کاملاً آفلاین است و اطلاعات روی دستگاه ذخیره می‌شود.",style=MaterialTheme.typography.bodyMedium)
        }
    }
}

class VM(app:Application):AndroidViewModel(app){
    private val db=AppDatabase.get(app); val classes=db.classDao().observeAll(); val settings=Settings(app)
    fun students(id:Long)=db.studentDao().observeByClass(id);fun attendance(d:String)=db.attendanceDao().observeByDate(d);fun studentAttendance(id:Long)=db.attendanceDao().observeByStudent(id)
    private fun io(b:suspend()->Unit)=CoroutineScope(Dispatchers.IO).launch{b()}
    fun addClass(n:String)=io{db.classDao().insert(SchoolClass(name=n))};fun deleteClass(c:SchoolClass)=io{db.classDao().delete(c)}
    fun addStudent(c:Long,n:String)=io{db.studentDao().insert(Student(classId=c,name=n))};fun deleteStudent(s:Student)=io{db.attendanceDao().deleteForStudent(s.id);db.studentDao().delete(s)}
    fun setAttendance(s:Long,d:String,x:String)=io{db.attendanceDao().upsert(Attendance(s,d,x))}
    fun exportExcel(c:SchoolClass,s:List<Student>)=io{val i=ExcelExport.create(getApplication(),c,s,db,settings.schoolName,settings.teacherName);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);getApplication<Application>().startActivity(Intent.createChooser(i,"ارسال فایل Excel"))}
    fun backup()=io{val i=Backup.create(getApplication(),db);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);getApplication<Application>().startActivity(Intent.createChooser(i,"پشتیبان‌گیری"))}
}
