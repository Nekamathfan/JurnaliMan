package tj.jurnali.man;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.zip.*;
import org.json.*;

public class MainActivity extends Activity {
    SharedPreferences prefs;
    LinearLayout content, root;
    String activeClassId, date;
    int lesson=1;
    final int BLUE=Color.rgb(25,118,210), NAVY=Color.rgb(14,27,45), GREEN=Color.rgb(35,150,80), RED=Color.rgb(205,45,45), YELLOW=Color.rgb(225,175,0);
    final int[] LESSONS={0,7,6,7,6,7,3,0};
    final String[] DAYS={"Якшанбе","Душанбе","Сешанбе","Чоршанбе","Панҷшанбе","Ҷумъа","Шанбе"};
    String pendingBackup="";
    TextView headerView;
    LinearLayout bottomNav;

    @Override public void onCreate(Bundle b){super.onCreate(b);
        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(Color.rgb(238,242,247));
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(true);
        }
        getWindow().getDecorView().setSystemUiVisibility(0);
        prefs=getSharedPreferences("jurnali_man",MODE_PRIVATE); date=new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()); if(!prefs.getBoolean("unlocked",false)) login(); else ensureData();}

    void login(){
        LinearLayout box=baseCol(); box.setPadding(35,70,35,30);
        TextView title=txt("📚  Журнали Ман",28,Color.WHITE); title.setGravity(Gravity.CENTER); title.setPadding(10,25,10,25); title.setBackgroundColor(NAVY); box.addView(title);
        TextView hint=txt("Журнали электронии давомоти хонандагон\nОфлайн • бе интернет",17,Color.DKGRAY); hint.setGravity(Gravity.CENTER); hint.setPadding(10,25,10,20); box.addView(hint);
        EditText p=new EditText(this); p.setHint("Парол"); p.setInputType(129); box.addView(p,new LinearLayout.LayoutParams(-1,60));
        Button go=button("Ворид шудан"); box.addView(go);
        go.setOnClickListener(v->{if("12345678".equals(p.getText().toString())){prefs.edit().putBoolean("unlocked",true).apply();ensureData();}else toast("Парол нодуруст аст");});
        setContentView(box);
    }
    LinearLayout baseCol(){LinearLayout x=new LinearLayout(this);x.setOrientation(LinearLayout.VERTICAL);x.setBackgroundColor(Color.WHITE);return x;}
    TextView txt(String s,float z,int c){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);return t;}
    Button button(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);return b;}

    void ensureData(){
        if(!prefs.contains("classes")){
            try{JSONArray cs=new JSONArray();JSONObject c=new JSONObject();c.put("id","class_8v");c.put("name","8 «В»");c.put("students",new JSONArray(new String[]{"АЛШУРОВ АБУБАКР","БАРАТОВА ЗИЁДА","БАРАТОВА МАДИНАБОНУ","БОБОНОЗАРОВА СОЛИҲА","ҒАНИЕВ СУНАТУЛЛО","ЗАРИПОВА СОФИЯ","ИБРОҲИМЗОДА ГЕСУ","ИШИМОВ АБДУСАЛОМ","КАРИМОВ ХУРШЕД","ҚАҲОРОВ МУҲАММАД","МАҲКАМОВ ИДРИС","МИНГБОЕВ БОБУР","МУХТОРОВ МУХАММАДЮСУФ","ОДИЛҶОНОВ ДЖАМОЛИДДИН","ОДИНАЕВ ИЛЁС","ПИРОВ БОБУРҶОН","ПУЛАТОВ КОМРОН","РАҲИМОВ УБАЙДУЛЛО","РУЗИЕВ МУХАММАД","САИДОВ РАБОНИ","САТТАРОВ ОТАҶОН","ТУРСУНОВА ШУКРОНА","УМАРОВ ФАРРУХ","ХАМРОЕВ БАРАКАТУЛЛО","ХОЛБОЕВА МЕҲРИНУШ","ХОЛОВ АБДУЛЛО","ХУДОЁРОВ ҲАСАН","ШЕРОВ САИДАҲМАДХОН","БЕРДИМУРАТОВ АЗИМҶОН","НАЗАРОВА ОСИЁ"})); cs.put(c); prefs.edit().putString("classes",cs.toString()).putString("activeClass","class_8v").apply();}catch(Exception e){}}
        activeClassId=prefs.getString("activeClass","class_8v"); main();
    }

    JSONArray classes(){try{return new JSONArray(prefs.getString("classes","[]"));}catch(Exception e){return new JSONArray();}}
    void saveClasses(JSONArray a){prefs.edit().putString("classes",a.toString()).apply();}
    JSONObject activeClass(){JSONArray a=classes();for(int i=0;i<a.length();i++)try{if(a.getJSONObject(i).getString("id").equals(activeClassId))return a.getJSONObject(i);}catch(Exception e){}return null;}
    JSONArray students(){try{return activeClass().getJSONArray("students");}catch(Exception e){return new JSONArray();}}
    void updateClass(JSONObject obj){JSONArray a=classes();for(int i=0;i<a.length();i++)try{if(a.getJSONObject(i).getString("id").equals(activeClassId)){a.put(i,obj);break;}}catch(Exception e){}saveClasses(a);}

    void main(){
        root=baseCol();
        headerView=txt("📚  Журнали Ман",24,Color.WHITE);headerView.setTypeface(null,Typeface.BOLD);headerView.setGravity(Gravity.CENTER_VERTICAL);headerView.setPadding(18,12,18,12);headerView.setBackgroundColor(NAVY);root.addView(headerView,new LinearLayout.LayoutParams(-1,72));
        LinearLayout classbar=new LinearLayout(this);classbar.setPadding(8,4,8,4);classbar.setGravity(Gravity.CENTER_VERTICAL);
        Spinner sp=new Spinner(this); ArrayAdapter<String> ad=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,classNames()); sp.setAdapter(ad); classbar.addView(sp,new LinearLayout.LayoutParams(0,58,1));
        Button manage=button("⚙ Синфҳо");classbar.addView(manage,new LinearLayout.LayoutParams(-2,58)); root.addView(classbar);
        sp.setSelection(indexOfActive()); sp.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){try{activeClassId=classes().getJSONObject(pos).getString("id");prefs.edit().putString("activeClass",activeClassId).apply();attendance();}catch(Exception e){}}});
        manage.setOnClickListener(v->classManager());
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(10,8,10,95);ScrollView sv=new ScrollView(this);sv.addView(content);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        bottomNav=new LinearLayout(this);bottomNav.setGravity(Gravity.CENTER);bottomNav.setPadding(5,4,5,4);bottomNav.setBackgroundColor(Color.rgb(238,242,247));
        Button a=navBtn("📝\nҚАЙД"), r=navBtn("📊\nҲИСОБОТ"), x=navBtn("📗\nБАРОВАРД\nБА EXCEL"), bk=navBtn("💾\nНУСХАИ\nЭҲТИЁТӢ"), rs=navBtn("♻️\nБАРҚАРОР\nКАРДАН");
        bottomNav.addView(a,new LinearLayout.LayoutParams(0,64,1));bottomNav.addView(r,new LinearLayout.LayoutParams(0,64,1));bottomNav.addView(x,new LinearLayout.LayoutParams(0,64,1));bottomNav.addView(bk,new LinearLayout.LayoutParams(0,64,1));bottomNav.addView(rs,new LinearLayout.LayoutParams(0,64,1));root.addView(bottomNav,new LinearLayout.LayoutParams(-1,72));
        a.setOnClickListener(v->attendance());r.setOnClickListener(v->report());x.setOnClickListener(v->excel());bk.setOnClickListener(v->backup());rs.setOnClickListener(v->restore());
        setContentView(root);
        applySystemInsets();
        attendance();
    }
    void applySystemInsets(){
        if(root==null)return;
        root.setOnApplyWindowInsetsListener((v,insets)->{
            int topInset=insets.getSystemWindowInsetTop();
            int bottomInset=insets.getSystemWindowInsetBottom();
            if(headerView!=null){
                headerView.setPadding(18,12+topInset,18,12);
                ViewGroup.LayoutParams hp=headerView.getLayoutParams();
                hp.height=72+topInset; headerView.setLayoutParams(hp);
            }
            if(bottomNav!=null){
                bottomNav.setPadding(5,4,5,bottomInset+4);
                ViewGroup.LayoutParams np=bottomNav.getLayoutParams();
                np.height=72+bottomInset; bottomNav.setLayoutParams(np);
            }
            return insets;
        });
        root.requestApplyInsets();
    }

    Button navBtn(String s){Button b=button(s);b.setTextSize(11);b.setGravity(Gravity.CENTER);b.setMinHeight(0);b.setMinWidth(0);b.setPadding(2,2,2,2);return b;}
    ArrayList<String> classNames(){ArrayList<String>x=new ArrayList<>();JSONArray a=classes();for(int i=0;i<a.length();i++)try{x.add(a.getJSONObject(i).getString("name"));}catch(Exception e){}return x;}
    int indexOfActive(){JSONArray a=classes();for(int i=0;i<a.length();i++)try{if(a.getJSONObject(i).getString("id").equals(activeClassId))return i;}catch(Exception e){}return 0;}

    Calendar cal(){String[] x=date.split("-");Calendar c=Calendar.getInstance();c.set(Integer.parseInt(x[0]),Integer.parseInt(x[1])-1,Integer.parseInt(x[2]));return c;}
    int dayLessons(){return LESSONS[cal().get(Calendar.DAY_OF_WEEK)-1];}
    String dayName(){return DAYS[cal().get(Calendar.DAY_OF_WEEK)-1];}
    String key(String sid){return "att|"+activeClassId+"|"+sid+"|"+date+"|"+lesson;}
    String status(String sid){return prefs.getString(key(sid),"");}
    String reason(String sid){return prefs.getString(key(sid)+"|reason","");}
    void put(String sid,String s,String why){prefs.edit().putString(key(sid),s).putString(key(sid)+"|reason",why==null?"":why).apply();}
    void clear(String sid){prefs.edit().remove(key(sid)).remove(key(sid)+"|reason").apply();}

    void attendance(){if(content==null)return;content.removeAllViews();
        LinearLayout top=new LinearLayout(this);top.setOrientation(LinearLayout.VERTICAL);
        Button d=button("📅  "+date);top.addView(d);d.setOnClickListener(v->pickDate());
        TextView inf=txt("Синф: "+activeClass().optString("name")+"\n"+(dayLessons()==0?dayName()+" — истироҳат":dayName()+" — "+dayLessons()+" дарс\nАгар қайд накунед — ҳозир ҳисоб мешавад."),15,Color.DKGRAY);inf.setPadding(8,6,8,8);top.addView(inf);
        if(dayLessons()>0){LinearLayout ls=new LinearLayout(this);for(int i=1;i<=dayLessons();i++){Button b=button("Дарс "+i);final int z=i;ls.addView(b,new LinearLayout.LayoutParams(0,50,1));b.setOnClickListener(v->{lesson=z;renderStudents();});}top.addView(ls);}
        content.addView(top);if(dayLessons()>0)renderStudents();
    }
    void pickDate(){Calendar c=cal();new DatePickerDialog(this,(v,y,m,d)->{date=String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d);lesson=1;attendance();},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}

    void renderStudents(){while(content.getChildCount()>1)content.removeViewAt(content.getChildCount()-1);if(dayLessons()==0)return;JSONArray ss=students();
        for(int i=0;i<ss.length();i++)try{
            JSONObject st=ss.getJSONObject(i);final String sid=st.getString("id");final int no=i+1;String nm=st.getString("name");
            LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(4,5,4,5);row.setBackgroundColor(Color.rgb(248,250,252));
            TextView name=txt(no+". "+nm,16,Color.rgb(25,35,50));name.setTypeface(null,Typeface.BOLD);name.setPadding(5,0,5,0);row.addView(name,new LinearLayout.LayoutParams(0,58,1));
            Button e=button("СБ"),u=button("БСБ"),cl=button("×");e.setTextSize(13);u.setTextSize(13);cl.setTextSize(16);row.addView(e,new LinearLayout.LayoutParams(55,52));row.addView(u,new LinearLayout.LayoutParams(60,52));row.addView(cl,new LinearLayout.LayoutParams(42,52));
            String s=status(sid);if("excused".equals(s)){e.setBackgroundColor(YELLOW);e.setTextColor(Color.WHITE);row.setBackgroundColor(Color.rgb(255,249,210));}else if("unexcused".equals(s)){u.setBackgroundColor(RED);u.setTextColor(Color.WHITE);row.setBackgroundColor(Color.rgb(255,228,228));}
            e.setOnClickListener(v->reasonDialog(sid));u.setOnClickListener(v->{put(sid,"unexcused","");renderStudents();});cl.setOnClickListener(v->{clear(sid);renderStudents();});
            content.addView(row); if(i<ss.length()-1){View line=new View(this);line.setBackgroundColor(Color.LTGRAY);content.addView(line,new LinearLayout.LayoutParams(-1,1));}
        }catch(Exception e){}
    }
    void reasonDialog(String sid){final EditText input=new EditText(this);input.setHint("Масалан: беморӣ");input.setText(reason(sid));new AlertDialog.Builder(this).setTitle("Сабаби ғоибӣ (СБ)").setView(input).setPositiveButton("Сабт",(d,w)->{put(sid,"excused",input.getText().toString());renderStudents();}).setNegativeButton("Бекор",null).show();}

    int monthScheduled(){Calendar c=Calendar.getInstance();String[] p=date.split("-");c.set(Integer.parseInt(p[0]),Integer.parseInt(p[1])-1,1);int y=c.get(Calendar.YEAR),m=c.get(Calendar.MONTH);int total=0;while(c.get(Calendar.YEAR)==y&&c.get(Calendar.MONTH)==m){total+=LESSONS[c.get(Calendar.DAY_OF_WEEK)-1];c.add(Calendar.DAY_OF_MONTH,1);}return total;}
    String monthPrefix(){return date.substring(0,7)+"-";}
    int count(String sid,String wanted){int n=0;for(Map.Entry<String,?> en:prefs.getAll().entrySet()){String k=en.getKey();if(k.startsWith("att|"+activeClassId+"|"+sid+"|"+monthPrefix())&&k.matches(".*\\|\\d+$")&&wanted.equals(en.getValue()))n++;}return n;}
    void report(){content.removeAllViews();TextView title=txt("📊 Ҳисоботи моҳона — "+activeClass().optString("name")+"\nМоҳ: "+date.substring(0,7),21,NAVY);title.setTypeface(null,Typeface.BOLD);title.setPadding(6,8,6,12);content.addView(title);
        int scheduled=monthScheduled();TextView info=txt("Дарсҳои нақшавӣ дар моҳ: "+scheduled+"\nҲозир = нақшавӣ − СБ − БСБ",15,Color.DKGRAY);info.setPadding(6,0,6,12);content.addView(info);
        JSONArray ss=students();for(int i=0;i<ss.length();i++)try{JSONObject st=ss.getJSONObject(i);String sid=st.getString("id");int e=count(sid,"excused"),u=count(sid,"unexcused"),p=Math.max(0,scheduled-e-u);double pct=scheduled==0?0:p*100.0/scheduled;TextView t=txt((i+1)+". "+st.getString("name")+"\n🟢 Ҳозир: "+p+"   🟡 СБ: "+e+"   🔴 БСБ: "+u+"\nҲозиршавӣ: "+String.format(Locale.US,"%.1f",pct)+"%",15,Color.DKGRAY);t.setPadding(8,9,8,9);content.addView(t);}catch(Exception e){}
    }

    void classManager(){
        final ArrayList<String> names=classNames();new AlertDialog.Builder(this).setTitle("Идоракунии синфҳо").setItems(names.toArray(new String[0]),(d,which)->studentManager(which)).setPositiveButton("＋ Синфи нав",(d,w)->newClassDialog()).setNegativeButton("Бекор",null).show();
    }
    void newClassDialog(){final EditText e=new EditText(this);e.setHint("Масалан: 7 «А»");new AlertDialog.Builder(this).setTitle("Синфи нав").setView(e).setPositiveButton("Сохтан",(d,w)->{String name=e.getText().toString().trim();if(name.isEmpty()){toast("Номи синфро нависед");return;}try{JSONArray a=classes();JSONObject c=new JSONObject();String id="c_"+UUID.randomUUID().toString();c.put("id",id);c.put("name",name);c.put("students",new JSONArray());a.put(c);saveClasses(a);activeClassId=id;prefs.edit().putString("activeClass",id).apply();main();}catch(Exception ex){toast("Хатогӣ");}}).setNegativeButton("Бекор",null).show();}
    void studentManager(int idx){try{JSONObject c=classes().getJSONObject(idx);activeClassId=c.getString("id");prefs.edit().putString("activeClass",activeClassId).apply();final ArrayList<String> opts=new ArrayList<>();opts.add("＋ Хонандаи нав");opts.add("✏️ Номи хонандаро таҳрир кардан");opts.add("🗑 Нест кардани хонанда");opts.add("🗑 Нест кардани ин синф");new AlertDialog.Builder(this).setTitle("Синф: "+c.getString("name")).setItems(opts.toArray(new String[0]),(d,w)->{if(w==0)addStudent();else if(w==1)editStudent();else if(w==2)deleteStudent();else deleteClass();}).setNegativeButton("Бекор",null).show();}catch(Exception e){}}
    void addStudent(){final EditText e=new EditText(this);e.setHint("Ному насаб");new AlertDialog.Builder(this).setTitle("Хонандаи нав").setView(e).setPositiveButton("Илова",(d,w)->{String n=e.getText().toString().trim();if(n.isEmpty())return;try{JSONObject c=activeClass();JSONArray s=c.getJSONArray("students");JSONObject st=new JSONObject();st.put("id","s_"+UUID.randomUUID().toString());st.put("name",n);s.put(st);updateClass(c);main();}catch(Exception x){}}).setNegativeButton("Бекор",null).show();}
    void editStudent(){JSONArray s=students();if(s.length()==0){toast("Хонанда нест");return;}String[] arr=new String[s.length()];for(int i=0;i<s.length();i++)try{arr[i]=(i+1)+". "+s.getJSONObject(i).getString("name");}catch(Exception e){}new AlertDialog.Builder(this).setTitle("Интихоби хонанда").setItems(arr,(d,w)->{try{JSONObject st=s.getJSONObject(w);final EditText e=new EditText(this);e.setText(st.getString("name"));new AlertDialog.Builder(this).setTitle("Тағйири ном").setView(e).setPositiveButton("Сабт",(d2,z)->{try{st.put("name",e.getText().toString().trim());updateClass(activeClass());main();}catch(Exception x){}}).setNegativeButton("Бекор",null).show();}catch(Exception x){}}).show();}
    void deleteStudent(){JSONArray s=students();if(s.length()==0){toast("Хонанда нест");return;}String[] arr=new String[s.length()];for(int i=0;i<s.length();i++)try{arr[i]=(i+1)+". "+s.getJSONObject(i).getString("name");}catch(Exception e){}new AlertDialog.Builder(this).setTitle("Нест кардани хонанда").setItems(arr,(d,w)->new AlertDialog.Builder(this).setTitle("Тасдиқ").setMessage("Ин хонанда ва қайдҳои ӯ нест карда мешаванд. Идома медиҳед?").setPositiveButton("Нест",(d2,z)->{try{s.remove(w);updateClass(activeClass());main();}catch(Exception x){}}).setNegativeButton("Бекор",null).show()).show();}
    void deleteClass(){if(classes().length()<=1){toast("Ақаллан як синф бояд монад");return;}new AlertDialog.Builder(this).setTitle("Нест кардани синф").setMessage("Синф ва рӯйхати он нест карда мешавад. Идома медиҳед?").setPositiveButton("Нест",(d,w)->{try{JSONArray a=classes();for(int i=0;i<a.length();i++)if(a.getJSONObject(i).getString("id").equals(activeClassId)){a.remove(i);break;}saveClasses(a);activeClassId=a.getJSONObject(0).getString("id");prefs.edit().putString("activeClass",activeClassId).apply();main();}catch(Exception e){}}).setNegativeButton("Бекор",null).show();}

    void backup(){try{JSONObject o=new JSONObject();o.put("version",1);JSONObject data=new JSONObject();for(Map.Entry<String,?> e:prefs.getAll().entrySet())if(e.getValue() instanceof String)data.put(e.getKey(),e.getValue());o.put("data",data);String f="JurnaliMan_Backup_"+new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(new Date())+".json";pendingBackup=o.toString();Intent in=new Intent(Intent.ACTION_CREATE_DOCUMENT);in.setType("application/json");in.putExtra(Intent.EXTRA_TITLE,f);startActivityForResult(in,10);}catch(Exception e){toast("Хатои нусха гирифтан");}}
    void restore(){Intent in=new Intent(Intent.ACTION_OPEN_DOCUMENT);in.setType("application/json");in.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(in,11);}
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(c!=RESULT_OK||d==null)return;try{if(r==10){OutputStream o=getContentResolver().openOutputStream(d.getData());o.write(pendingBackup.getBytes("UTF-8"));o.close();toast("Нусха нигоҳ дошта шуд");}else if(r==12){OutputStream o=getContentResolver().openOutputStream(d.getData());o.write(pendingXlsx);o.close();toast("Excel омода шуд");}else{BufferedReader br=new BufferedReader(new InputStreamReader(getContentResolver().openInputStream(d.getData()),"UTF-8"));StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line);br.close();JSONObject o=new JSONObject(sb.toString());JSONObject data=o.getJSONObject("data");SharedPreferences.Editor ed=prefs.edit();Iterator<String> it=data.keys();while(it.hasNext()){String k=it.next();ed.putString(k,data.getString(k));}ed.apply();activeClassId=prefs.getString("activeClass",activeClassId);toast("Маълумот барқарор шуд");main();}}catch(Exception e){toast("Файли нусха нодуруст аст");}}

    void excel(){try{String file="JurnaliMan_"+activeClass().optString("name").replace(" ","_")+"_"+date.substring(0,7)+".xlsx";pendingXlsx=makeXlsx();Intent in=new Intent(Intent.ACTION_CREATE_DOCUMENT);in.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");in.putExtra(Intent.EXTRA_TITLE,file);startActivityForResult(in,12);}catch(Exception e){toast("Хатои Excel");}}
    byte[] pendingXlsx;
    byte[] makeXlsx() throws Exception{
        ByteArrayOutputStream 
