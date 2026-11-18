package br.com.zapia.ch8;

import android.app.*;import android.os.*;import android.graphics.*;import android.view.*;import android.widget.*;import android.graphics.drawable.GradientDrawable;import java.util.*;

public class MainActivity extends Activity {
  final int BG=Color.rgb(7,15,12), PANEL=Color.rgb(18,34,27), GREEN=Color.rgb(108,255,146), MINT=Color.rgb(190,255,207); LinearLayout root;
  public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);library();}
  GradientDrawable bg(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(r);d.setStroke(2,GREEN);return d;}
  TextView label(String s,int z,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(color);t.setGravity(Gravity.CENTER);return t;}
  void library(){root=new LinearLayout(this);root.setOrientation(1);root.setPadding(18,12,18,8);root.setBackgroundColor(BG);setContentView(root);
    TextView title=label("ARCADE RETRÔ",28,GREEN);title.setTypeface(Typeface.MONOSPACE,1);root.addView(title,new LinearLayout.LayoutParams(-1,58));
    TextView sub=label("30 JOGOS ORIGINAIS • OFFLINE • SEM ROMS",12,MINT);sub.setTypeface(Typeface.MONOSPACE,1);root.addView(sub,new LinearLayout.LayoutParams(-1,30));
    TextView desc=label("Escolha um jogo. Controles virtuais dentro de cada partida.",13,Color.LTGRAY);root.addView(desc,new LinearLayout.LayoutParams(-1,36));
    ScrollView scroll=new ScrollView(this);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));LinearLayout list=new LinearLayout(this);list.setOrientation(1);scroll.addView(list);
    for(int i=0;i<30;i++){final int n=i;TextView item=label(String.format(Locale.US,"%02d   %s\n%s",i+1,ARCADE_NAMES[i],ARCADE_HINTS[i/5]),16,MINT);item.setGravity(Gravity.CENTER_VERTICAL);item.setPadding(16,4,10,4);item.setTypeface(Typeface.MONOSPACE,1);item.setBackground(bg(PANEL,16));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,62);lp.setMargins(0,4,0,4);list.addView(item,lp);item.setOnClickListener(v->play(n));}
  }
  void play(int n){LinearLayout game=new LinearLayout(this);game.setOrientation(1);game.setPadding(12,6,12,8);game.setBackgroundColor(BG);setContentView(game);
    Arcade view=new Arcade(n);TextView bar=label("‹  JOGOS                 "+ARCADE_NAMES[n],16,GREEN);bar.setTypeface(Typeface.MONOSPACE,1);game.addView(bar,new LinearLayout.LayoutParams(-1,48));bar.setOnClickListener(v->{view.alive=false;current=null;library();});
    game.addView(view,new LinearLayout.LayoutParams(-1,0,1));
    TextView how=label(ARCADE_HINTS[n/5]+"   •   TOQUE NO DIRECIONAL E AÇÃO",12,MINT);game.addView(how,new LinearLayout.LayoutParams(-1,34));
    LinearLayout controls=new LinearLayout(this);controls.setGravity(Gravity.CENTER);game.addView(controls,new LinearLayout.LayoutParams(-1,190));
    LinearLayout d=new LinearLayout(this);d.setOrientation(1);d.setGravity(Gravity.CENTER);controls.addView(d,new LinearLayout.LayoutParams(220,180));button(d,"▲",0, -1);LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER);d.addView(row);button(row,"◀",1,0);TextView gap=label("",12,GREEN);row.addView(gap,new LinearLayout.LayoutParams(54,54));button(row,"▶",2,0);button(d,"▼",3,-1);
    LinearLayout act=new LinearLayout(this);act.setGravity(Gravity.CENTER);controls.addView(act,new LinearLayout.LayoutParams(100,100));button(act,"A",4,0);
  }
  void button(LinearLayout parent,String s,int code,int unused){TextView t=label(s,24,GREEN);t.setTypeface(Typeface.MONOSPACE,1);t.setBackground(bg(PANEL,24));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(58,54);p.setMargins(4,3,4,3);parent.addView(t,p);t.setOnTouchListener((v,e)->{if(e.getAction()==0){if(current!=null)current.input(code);return true;}return e.getAction()==1||e.getAction()==3;});}
  Arcade current; @Override public void onBackPressed(){if(current!=null){current.alive=false;current=null;library();}else super.onBackPressed();}

  static final String[] ARCADE_NAMES={"Pixel Orchard","Gem Garden","Star Courier","Moon Pebbles","Neon Harvest","Orbit Escape","Meteor Shift","Laser Alley","Comet Run","Night Dodger","Astro Blaster","Bug Buster","Sky Patrol","Pixel Defender","Cosmic Courier","Brick Bloom","Neon Breaker","Gem Bouncer","Wall Wizard","Prism Smash","Pocket Serpent","Neon Snake","Apple Loop","Byte Eater","Circuit Worm","Tiny Labyrinth","Crystal Maze","Key Runner","Shadow Passage","Exit 8-Bit"};
  static final String[] ARCADE_HINTS={"COLETE OS PONTOS • DESVIE DOS BLOCOS","DESVIE E SOBREVIVA O MÁXIMO","MOVA E ATIRE COM AÇÃO","REBATA A BOLA E QUEBRE OS BLOCOS","SERPENTE: COMA E CRESÇA","LABIRINTO: PEGUE A CHAVE E SAIA"};
  class Arcade extends View {
    
    
    final int id,genre,variant; final Paint p=new Paint(3);final Random random;final Handler h=new Handler();boolean alive=true,win=false;int px=9,py=10,score=0,ticks=0,lives=3,dx=0,dy=0;float bx=9,by=8,bvx=.43f,bvy=.36f;int paddle=9;boolean[][] wall=new boolean[20][14],brick=new boolean[20][4];ArrayList<int[]> dots=new ArrayList<>(),foes=new ArrayList<>(),body=new ArrayList<>(),shots=new ArrayList<>();int foodX=4,foodY=5,keyX=17,keyY=11;boolean hasKey=false,action=false;long last=0;
    Arcade(int n){super(MainActivity.this);id=n;genre=n/5;variant=n%5;random=new Random(1700+n);current=this;setLayerType(View.LAYER_TYPE_SOFTWARE,null);init();h.post(loop);}
    void init(){if(genre==0){for(int j=0;j<5+variant;j++){int x=random.nextInt(18)+1,y=random.nextInt(11)+1;dots.add(new int[]{x,y});}for(int j=0;j<10+variant*2;j++){int x=random.nextInt(18)+1,y=random.nextInt(11)+1;if(x!=px||y!=py)wall[x][y]=true;}}
      if(genre==1){for(int j=0;j<3+variant;j++)foes.add(new int[]{random.nextInt(18)+1,random.nextInt(9)+1});}
      if(genre==2){for(int j=0;j<5+variant;j++)foes.add(new int[]{2+j*3,2+random.nextInt(3)});}
      if(genre==3){for(int x=1;x<19;x++)for(int y=1;y<4;y++)brick[x][y]=((x+y+variant)% (2+variant%3)!=0);}
      if(genre==4){body.add(new int[]{px,py});body.add(new int[]{px-1,py});body.add(new int[]{px-2,py});foodX=3+random.nextInt(14);foodY=3+random.nextInt(8);}
      if(genre==5){for(int x=0;x<20;x++){wall[x][0]=wall[x][13]=true;}for(int y=0;y<14;y++){wall[0][y]=wall[19][y]=true;}for(int x=3;x<17;x+=3){for(int y=2;y<12;y++)if((x+y+variant)%5!=0)wall[x][y]=true;}wall[1][1]=false;wall[18][12]=false;}}
    void input(int key){if(key==0){dx=0;dy=-1;}if(key==1){dx=-1;dy=0;}if(key==2){dx=1;dy=0;}if(key==3){dx=0;dy=1;}if(key==4){action=true;}}
    Runnable loop=new Runnable(){public void run(){if(!alive)return;long now=System.currentTimeMillis();if(now-last>130){last=now;tick();}invalidate();h.postDelayed(this,25);}};
    void tick(){if(win)return;ticks++;if(genre==0)moveCollector();if(genre==1)moveDodger();if(genre==2)moveShooter();if(genre==3)moveBreakout();if(genre==4)moveSnake();if(genre==5)moveMaze();action=false;}
    void moveCollector(){px=clamp(px+dx,1,18);py=clamp(py+dy,1,12);dx=dy=0;if(wall[px][py]){px=1;py=1;lives--;if(lives<=0)resetPos();}for(int i=dots.size()-1;i>=0;i--)if(dots.get(i)[0]==px&&dots.get(i)[1]==py){dots.remove(i);score++;}if(dots.isEmpty())win=true;}
    void moveDodger(){px=clamp(px+dx,1,18);dx=0;if(ticks%3==0){for(int[] f:foes){f[1]++;if(f[1]>12){f[1]=1;f[0]=random.nextInt(18)+1;}if(f[0]==px&&f[1]==11){lives--;f[1]=1;if(lives<=0)resetPos();}}}if(ticks>40+variant*4){score+=lives*10;win=true;}}
    void moveShooter(){px=clamp(px+dx,1,18);dx=0;if(action){shots.add(new int[]{px,10});}for(int i=shots.size()-1;i>=0;i--){int[] s=shots.get(i);s[1]--;if(s[1]<1){shots.remove(i);continue;}for(int j=foes.size()-1;j>=0;j--){int[] f=foes.get(j);if(s[0]==f[0]&&s[1]==f[1]){foes.remove(j);shots.remove(i);score++;break;}}}if(ticks%5==0)for(int[] f:foes){f[0]+=random.nextBoolean()?1:-1;f[0]=clamp(f[0],1,18);}if(foes.isEmpty())win=true;}
    void moveBreakout(){paddle=clamp(paddle+dx,2,17);dx=0;bx+=bvx;by+=bvy;if(bx<1||bx>18)bvx=-bvx;if(by<1)bvy=-bvy;if(by>10){if(Math.abs(bx-paddle)<2.3){bvy=-Math.abs(bvy);score++;}else{lives--;bx=9;by=8;bvy=-.36f;if(lives<=0)resetPos();}}int ix=Math.round(bx),iy=Math.round(by);if(iy>=1&&iy<4&&ix>=1&&ix<19&&brick[ix][iy]){brick[ix][iy]=false;bvy=-bvy;score++;}boolean remains=false;for(int x=1;x<19;x++)for(int y=1;y<4;y++)if(brick[x][y])remains=true;if(!remains)win=true;}
    void moveSnake(){int nx=clamp(px+dx,1,18),ny=clamp(py+dy,1,12);dx=dy=0;for(int[] b:body)if(b[0]==nx&&b[1]==ny){body.clear();body.add(new int[]{9,10});px=9;py=10;lives--;if(lives<=0)resetPos();return;}px=nx;py=ny;body.add(0,new int[]{px,py});if(px==foodX&&py==foodY){score++;foodX=1+random.nextInt(18);foodY=1+random.nextInt(12);if(score>=8+variant)win=true;}else if(body.size()>3+score)body.remove(body.size()-1);}
    void moveMaze(){int nx=clamp(px+dx,1,18),ny=clamp(py+dy,1,12);dx=dy=0;if(!wall[nx][ny]){px=nx;py=ny;}if(px==keyX&&py==keyY)hasKey=true;if(px==18&&py==12&&hasKey){score=100;win=true;}if(ticks>500){ticks=0;}}
    void resetPos(){px=9;py=10;lives=3;}
    int clamp(int v,int a,int b){return Math.max(a,Math.min(b,v));}
    protected void onDraw(Canvas c){super.onDraw(c);c.drawColor(Color.rgb(3,10,7));float cell=Math.min(getWidth()/20f,getHeight()/16f);float ox=(getWidth()-20*cell)/2f,oy=(getHeight()-16*cell)/2f;p.setColor(Color.rgb(13,25,18));c.drawRoundRect(ox-5,oy-5,ox+20*cell+5,oy+16*cell+5,12,12,p);
      p.setColor(GREEN);p.setTypeface(Typeface.MONOSPACE);p.setTextSize(cell*.72f);c.drawText(ARCADE_NAMES[id],ox,oy-cell*.9f,p);p.setTextSize(cell*.55f);p.setColor(MINT);c.drawText("PUNTOS "+score+"     VIDAS "+lives,ox,oy+16*cell+cell*.9f,p);
      p.setColor(Color.rgb(30,55,38));for(int x=0;x<20;x++)for(int y=0;y<14;y++){float l=ox+x*cell,t=oy+y*cell;if(wall[x][y]){p.setColor(Color.rgb(39,76,48));c.drawRoundRect(l+1,t+1,l+cell-1,t+cell-1,3,3,p);}else if(genre==3&&y<4&&brick[x][y]){p.setColor(Color.rgb(52+((x+y)%3)*25,190,98));c.drawRoundRect(l+1,t+1,l+cell-1,t+cell-1,3,3,p);}}
      if(genre==0){p.setColor(Color.YELLOW);for(int[] d:dots)dot(c,ox,oy,cell,d[0],d[1]);}if(genre==1||genre==2){p.setColor(Color.RED);for(int[] f:foes)dot(c,ox,oy,cell,f[0],f[1]);}if(genre==2){p.setColor(Color.CYAN);for(int[] s:shots)dot(c,ox,oy,cell,s[0],s[1]);}
      if(genre==3){p.setColor(Color.CYAN);c.drawRoundRect(ox+(bx-.3f)*cell,oy+(by-.3f)*cell,ox+(bx+.3f)*cell,oy+(by+.3f)*cell,cell,cell,p);p.setColor(GREEN);c.drawRoundRect(ox+(paddle-1.4f)*cell,oy+11*cell,ox+(paddle+1.4f)*cell,oy+11.5f*cell,4,4,p);}
      if(genre==4){p.setColor(GREEN);for(int[] b:body)dot(c,ox,oy,cell,b[0],b[1]);p.setColor(Color.YELLOW);dot(c,ox,oy,cell,foodX,foodY);}
      if(genre==5){p.setColor(Color.YELLOW);if(!hasKey)dot(c,ox,oy,cell,keyX,keyY);p.setColor(Color.GREEN);dot(c,ox,oy,cell,18,12);}
      if(genre!=3&&genre!=4){p.setColor(Color.CYAN);dot(c,ox,oy,cell,px,py);}if(win){p.setColor(0xCC00150A);c.drawRect(ox,oy,ox+20*cell,oy+14*cell,p);p.setColor(GREEN);p.setTextSize(cell*1.4f);c.drawText("VENCEU!",ox+cell*4,oy+cell*7,p);p.setTextSize(cell*.65f);c.drawText("Toque em JOGOS para voltar",ox+cell*2,oy+cell*8.5f,p);}}
    void dot(Canvas c,float ox,float oy,float cell,int x,int y){c.drawRoundRect(ox+x*cell+2,oy+y*cell+2,ox+(x+1)*cell-2,oy+(y+1)*cell-2,cell*.22f,cell*.22f,p);}
    public boolean onTouchEvent(android.view.MotionEvent e){if(e.getAction()==MotionEvent.ACTION_UP){if(genre==2)action=true;return true;}return true;}
  }
}
