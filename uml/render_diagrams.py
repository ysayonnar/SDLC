"""Rebuild the five Ritmo diagrams with exact, shared geometry.
Requires ReportLab, the specified TrueType fonts and Poppler (pdftoppm).
Run from any directory: python uml/render_diagrams.py
"""
from pathlib import Path
import math, html, xml.etree.ElementTree as E
from reportlab.pdfgen import canvas
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.lib.units import mm
ROOT=Path(__file__).resolve().parents[1]; OUT=ROOT/'uml'; QA=Path(__import__('tempfile').mkdtemp(prefix='ritmo-uml-'))
FONTS=Path('/System/Library/Fonts/Supplemental')
for name,file in [('Arial','Arial.ttf'),('ArialBold','Arial Bold.ttf'),('Narrow','Arial Narrow Italic.ttf'),('Times','Times New Roman.ttf'),('TimesBold','Times New Roman Bold.ttf')]:pdfmetrics.registerFont(TTFont(name,str(FONTS/file)))
class Sheet:
 def __init__(self,name,w,h):
  self.name,self.w,self.h=name,w,h;self.svg=[];self.errors=[];self.c=canvas.Canvas(str(QA/(name+'.pdf')),pagesize=(w*mm,h*mm));self.c.setTitle('Ритмо — '+name);self.c.setAuthor('Нагорный Г. Д.')
  self.mx=E.Element('mxfile',host='app.diagrams.net');dia=E.SubElement(self.mx,'diagram',name='Ритмо');g=E.SubElement(dia,'mxGraphModel',page='1',pageWidth=str(w*4),pageHeight=str(h*4));self.root=E.SubElement(g,'root');E.SubElement(self.root,'mxCell',id='0');E.SubElement(self.root,'mxCell',id='1',parent='0');self.n=1
 def cell(self,x,y,w,h,label='',style=''):
  self.n+=1;node=E.SubElement(self.root,'mxCell',id=str(self.n),value=label,style='strokeColor=#000000;fillColor=#ffffff;strokeWidth=1.2;fontFamily=Arial;fontColor=#000000;whiteSpace=wrap;html=0;'+style,vertex='1',parent='1');E.SubElement(node,'mxGeometry',x=str(x*4),y=str(y*4),width=str(w*4),height=str(h*4),**{'as':'geometry'})
 def line(self,pts,dash=False,arrow=None,lw=.3):
  c=self.c;c.setLineWidth(lw*mm);c.setStrokeColorRGB(0,0,0);c.setDash([2*mm,1.2*mm] if dash else [])
  p=c.beginPath();p.moveTo(pts[0][0]*mm,(self.h-pts[0][1])*mm)
  for x,y in pts[1:]:p.lineTo(x*mm,(self.h-y)*mm)
  c.drawPath(p);self.svg.append('<polyline points="'+' '.join(f'{x},{y}' for x,y in pts)+'" fill="none" stroke="black" stroke-width="'+str(lw)+'"'+(' stroke-dasharray="2 1.2"' if dash else '')+'/>')
  if arrow:
   a,b=pts[-2:];ang=math.atan2(b[1]-a[1],b[0]-a[0]);length=2.5 if self.w<200 else 3.5;half=length*math.tan(math.pi/6)
   aa=(b[0]-length*math.cos(ang)+half*math.sin(ang),b[1]-length*math.sin(ang)-half*math.cos(ang));bb=(b[0]-length*math.cos(ang)-half*math.sin(ang),b[1]-length*math.sin(ang)+half*math.cos(ang))
   if arrow=='filled':self.poly([aa,b,bb],fill='black',lw=lw,record=False)
   else:self.line([aa,b,bb],lw=lw)
  self.n+=1;n=E.SubElement(self.root,'mxCell',id=str(self.n),style=f'edgeStyle=none;endArrow={"block" if arrow=="filled" else "none"};endFill={int(arrow=="filled")};dashed={int(dash)};strokeWidth={lw*4};strokeColor=#000000;',edge='1',parent='1');g=E.SubElement(n,'mxGeometry',relative='1',**{'as':'geometry'});E.SubElement(g,'mxPoint',x=str(pts[0][0]*4),y=str(pts[0][1]*4),**{'as':'sourcePoint'});E.SubElement(g,'mxPoint',x=str(pts[-1][0]*4),y=str(pts[-1][1]*4),**{'as':'targetPoint'});arr=E.SubElement(g,'Array',**{'as':'points'})
  for x,y in pts[1:-1]:E.SubElement(arr,'mxPoint',x=str(x*4),y=str(y*4))
 def rect(self,x,y,w,h,r=0,lw=.3):
  c=self.c;c.setDash([]);c.setLineWidth(lw*mm);c.setFillColorRGB(1,1,1);c.setStrokeColorRGB(0,0,0)
  if r:c.roundRect(x*mm,(self.h-y-h)*mm,w*mm,h*mm,r*mm,stroke=1,fill=1)
  else:c.rect(x*mm,(self.h-y-h)*mm,w*mm,h*mm,stroke=1,fill=1)
  self.svg.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}" stroke="black" stroke-width="{lw}" fill="white"/>');self.cell(x,y,w,h,style=f'rounded={int(bool(r))};arcSize=15;strokeWidth={lw*4};')
 def ellipse(self,x,y,w,h,fill='white',lw=.3):
  c=self.c;c.setDash([]);c.setLineWidth(lw*mm);c.setFillColorRGB(*((0,0,0) if fill=='black' else (1,1,1)));c.ellipse(x*mm,(self.h-y-h)*mm,(x+w)*mm,(self.h-y)*mm,stroke=1,fill=1)
  self.svg.append(f'<ellipse cx="{x+w/2}" cy="{y+h/2}" rx="{w/2}" ry="{h/2}" stroke="black" stroke-width="{lw}" fill="{fill}"/>');self.cell(x,y,w,h,style=f'ellipse;fillColor={fill};')
 def poly(self,pts,fill='white',lw=.3,record=True,shape='rhombus'):
  c=self.c;c.setDash([]);c.setLineWidth(lw*mm);c.setFillColorRGB(*((0,0,0) if fill=='black' else (1,1,1)));p=c.beginPath();p.moveTo(pts[0][0]*mm,(self.h-pts[0][1])*mm)
  for x,y in pts[1:]:p.lineTo(x*mm,(self.h-y)*mm)
  p.close();c.drawPath(p,stroke=1,fill=1);self.svg.append('<polygon points="'+' '.join(f'{x},{y}' for x,y in pts)+f'" stroke="black" stroke-width="{lw}" fill="{fill}"/>')
  if record:
   xx=[p[0] for p in pts];yy=[p[1] for p in pts];self.cell(min(xx),min(yy),max(xx)-min(xx),max(yy)-min(yy),style=f'shape={shape};fillColor={fill};')
 def text(self,x,y,text,size=5.2,bold=False,align='center',font=None,maxwidth=None,rotate=0):
  font=font or ('ArialBold' if bold else 'Arial');lines=text.split('\n');lh=size*1.13;c=self.c
  for i,s in enumerate(lines):
   width=pdfmetrics.stringWidth(s,font,size*mm)/mm
   if maxwidth is not None and width>maxwidth:self.errors.append(f'{self.name}: {s}: {width:.1f}>{maxwidth}')
   yy=y+(i-(len(lines)-1)/2)*lh;c.saveState();c.translate(x*mm,(self.h-yy)*mm);c.rotate(rotate);c.setFillColorRGB(0,0,0);c.setFont(font,size*mm)
   baseline=-size*mm*.34
   if align=='center':c.drawCentredString(0,baseline,s)
   elif align=='right':c.drawRightString(0,baseline,s)
   else:c.drawString(0,baseline,s)
   c.restoreState();anchor='middle' if align=='center' else 'end' if align=='right' else 'start';self.svg.append(f'<text x="{x}" y="{yy}" font-family="{'Times New Roman' if font.startswith('Times') else 'Arial Narrow' if font=='Narrow' else 'Arial'}" font-size="{size}" text-anchor="{anchor}" dominant-baseline="central"'+(' font-weight="bold"' if bold else '')+(f' transform="rotate({-rotate},{x},{yy})"' if rotate else '')+'>'+html.escape(s)+'</text>')
  width=maxwidth or max(10,max(pdfmetrics.stringWidth(s,font,size*mm)/mm for s in lines));left=x-width/2 if align=='center' else x-width if align=='right' else x
  self.cell(left,y-lh*len(lines)/2,width,lh*len(lines),text,style=f'text;strokeColor=none;fillColor=none;fontFamily={'Times New Roman' if font.startswith('Times') else 'Arial Narrow' if font=='Narrow' else 'Arial'};fontSize={size*4};fontStyle={int(bold)};align={align};rotation={-rotate};')
 def block(self,y,label,cx=82,w=60,h=20,kind='process',font=5.2):
  x=cx-w/2
  if kind=='decision':self.poly([(cx,y),(x+w,y+h/2),(cx,y+h),(x,y+h/2)])
  elif kind=='data':self.poly([(x+5,y),(x+w,y),(x+w-5,y+h),(x,y+h)],shape='parallelogram')
  else:self.rect(x,y,w,h,r=h/2 if kind=='terminal' else 0)
  if kind=='predefined':self.line([(x+4,y),(x+4,y+h)]);self.line([(x+w-4,y),(x+w-4,y+h)])
  self.text(cx,y+h/2,label,font,maxwidth=w-10)
 def actor(self,x,y,label,size=9):
  self.ellipse(x-3,y-14,6,6,lw=.5);self.line([(x,y-8),(x,y+5)],lw=.5);self.line([(x-6,y-3),(x+6,y-3)],lw=.5);self.line([(x-6,y+14),(x,y+5),(x+6,y+14)],lw=.5);self.text(x,y+24,label,size,align='right')
 def save(self):
  if self.errors:raise ValueError('\n'.join(self.errors))
  self.c.showPage();self.c.save();(OUT/'images'/f'{self.name}.svg').write_text(f'<svg xmlns="http://www.w3.org/2000/svg" width="{self.w}mm" height="{self.h}mm" viewBox="0 0 {self.w} {self.h}">'+''.join(self.svg)+'</svg>');E.ElementTree(self.mx).write(OUT/'models'/f'{self.name}.drawio',encoding='utf-8',xml_declaration=True)
# 3.1 Standard UML; each relation has its own line and endpoint.
s=Sheet('use-cases',330,345);s.rect(110,3,218,338,lw=.6);s.text(225,14,'Ритмо',11,True);s.actor(74,39,'Гость',10);s.actor(74,193,'Пользователь',10)
ys=[33,65,97,129,161,193,225,257,289,321]
texts=['UC-01\nРегистрация','UC-02 Войти\nв систему','UC-03 Создать\nпривычку','UC-04 Изменить\nназвание','UC-05 Удалить\nпривычку','UC-06 Отметить\nсегодня','UC-07 Отменить\nотметку','UC-08 Показать\nпрогресс','UC-09 Выгрузить\nCSV','UC-10 Выйти\nиз системы']
for i,(y,t) in enumerate(zip(ys,texts)):
 s.ellipse(120,y-15,98,30,lw=.6);s.text(169,y,t,9.7,maxwidth=80);s.line([(80,36 if i<2 else 190),(120,y)],lw=.6)
s.ellipse(264,180,62,58,lw=.6);s.text(295,209,'UC-11\nРасчёт\nпрогресса',10,maxwidth=53)
for start,end in [(161,(270,191)),(193,(264.6,203)),(225,(264.6,215)),(257,(270,227))]:
 s.line([(218,start),end],dash=True,arrow='open',lw=.6);dx=end[0]-218;dy=end[1]-start;length=math.hypot(dx,dy);s.text((218+end[0])/2+dy/length*8,(start+end[1])/2-dx/length*8,'«include»',9.9,rotate=-math.degrees(math.atan2(dy,dx)))
s.save()
# 3.2 GOST flowchart: no arrows for the normal down/right flow; open 60-degree arrows for returns.
s=Sheet('activity-create-habit',165,216)
for y,label,h,kind in [(4,'Начало',10,'terminal'),(22,'Ввод названия',20,'data'),(50,'Сессия\nи CSRF?',24,'decision'),(84,'Имя\nкорректно?',24,'decision'),(118,'Сохранить\nпривычку',20,'process'),(148,'Показать\nпривычку',20,'data'),(186,'Конец',10,'terminal')]:s.block(y,label,h=h,w=40 if kind=='terminal' else 57 if h==19 else 60,kind=kind)
for a,b in [(14,22),(42,50),(74,84),(108,118),(138,148),(168,186)]:s.line([(82,a),(82,b)])
s.text(89,79,'Да',4.9);s.text(89,113,'Да',4.9)
s.block(52,'Отказ\n401 / 403',cx=140,w=40,h=20);s.line([(112,62),(120,62)]);s.text(116,48,'Нет',4.9)
s.line([(140,72),(140,191),(102,191)],arrow='open')
s.block(86,'Ошибка\nимени',cx=24,w=40,h=20);s.line([(52,96),(44,96)],arrow='open');s.text(48,82,'Нет',4.9)
s.line([(24,86),(24,32),(54.5,32)],arrow='open')
s.save()
# 3.3 Same GOST geometry; failure branches merge at a marked flow junction.
s=Sheet('activity-complete-today',165,230)
for y,label,h,kind in [(3,'Начало',10,'terminal'),(17,'Запрос отметки',19,'data'),(40,'Проверить\nдоступ',19,'predefined'),(63,'Доступ\nразрешён?',24,'decision'),(94,'Определить\nдату',19,'process'),(117,'Дата\nсовпадает?',24,'decision'),(148,'Записать\nотметку',19,'process'),(171,'Рассчитать\nпрогресс',19,'predefined'),(194,'Зафиксировать\nи показать',19,'data'),(217,'Конец',10,'terminal')]:s.block(y,label,h=h,w=40 if kind=='terminal' else 57 if h==19 else 60,kind=kind)
for a,b in [(13,17),(36,40),(59,63),(87,94),(113,117),(141,148),(167,171),(190,194),(213,217)]:s.line([(82,a),(82,b)])
s.text(89,90.5,'Да',4.9);s.text(89,144.5,'Да',4.9)
s.block(65,'Отказ\n401/403/404',cx=24,w=40,h=20);s.line([(52,75),(44,75)],arrow='open');s.text(48,61,'Нет',4.9)
s.block(119,'Обновить\nсписок',cx=24,w=40,h=20);s.line([(52,129),(44,129)],arrow='open');s.text(48,115,'Нет',4.9)
s.line([(24,85),(24,93),(3,93)],arrow='open');s.line([(3,93),(3,222),(62,222)]);s.line([(24,139),(24,147),(3,147)],arrow='open');s.ellipse(2.3,146.3,1.4,1.4,fill='black');s.save()
# 3.4 UML state machine for a fixed habit and date d: no calendar projection mixed into history.
s=Sheet('state-habit',165,193);s.ellipse(79.5,3,5,5,fill='black');s.line([(82,8),(82,34)],arrow='open');s.text(89,21,'создать',5.2,align='left')
s.rect(39.5,34,85,24,r=3);s.text(82,46,'Нет отметки\nза дату d');s.rect(39.5,92,85,24,r=3);s.text(82,104,'Есть отметка\nза дату d');s.rect(39.5,148,85,20,r=3);s.text(82,158,'Привычка удалена')
s.line([(82,58),(82,92)],arrow='open');s.text(89,75,'отметить\n[дата верна]',5.2,align='left')
s.line([(39.5,104),(11,104),(11,46),(39.5,46)],arrow='open');s.text(18,76,'отменить\n[дата верна]',5.2,align='left')
s.line([(124.5,46),(155,46),(155,158),(124.5,158)],arrow='open');s.text(140,39,'удалить',5.2)
s.line([(82,116),(82,148)],arrow='open');s.text(89,132,'удалить',5.2,align='left')
s.ellipse(78.5,181,7,7);s.ellipse(80,182.5,4,4,fill='black');s.line([(82,168),(82,181)],arrow='open');s.save()
# A3: one successful interaction, four participants and eleven short messages.
s=Sheet('sequence-complete-today-a3',420,297);s.rect(20,5,395,287,lw=.7)
s.rect(20,5,70,14,lw=.7);s.text(55,12,'БГУИР ЛР 6-05-0611-05 16 ПД',3.8,font='Narrow',rotate=180,maxwidth=68)
s.text(220,10,'ПРИЛОЖЕНИЕ В',4.94,True,font='TimesBold');s.text(220,17,'(обязательное)',4.94,font='Times');s.text(220,24,'Диаграмма последовательности ежедневной отметки',4.94,True,font='TimesBold')
xs=[60,145,245,355]
# Actor is completely below the identification field.
s.ellipse(57,42,6,6);s.line([(60,48),(60,56)]);s.line([(54,51),(66,51)]);s.line([(55,61),(60,56),(65,61)]);s.text(60,67,'Пользователь',5.2)
for x,label in zip(xs[1:],['React\nВеб-интерфейс','Java API\nСервис отметок','PostgreSQL\nБаза данных']):s.rect(x-33,41,66,20);s.text(x,51,label,5.2,maxwidth=60)
for x in xs:s.line([(x,72 if x==60 else 61),(x,228)],dash=True)
s.rect(143.5,80,3,146);s.rect(243.5,94,3,118)
for top in [108,136,184]:s.rect(353.5,top,3,14)
def msg(i,a,b,y,label,ret=False):
 s.line([(xs[a]+(1.5 if b>a else -1.5),y),(xs[b]+(-1.5 if b>a else 1.5),y)],dash=ret,arrow='open' if ret else 'filled');s.text((xs[a]+xs[b])/2,y-4,str(i)+' '+label,5.2)
msg(1,0,1,80,'Нажать «Выполнено»');msg(2,1,2,94,'Отметить(expectedDate)');msg(3,2,3,108,'Найти и заблокировать');msg(4,3,2,122,'Привычка',True);msg(5,2,3,136,'Записать и прочитать даты');msg(6,3,2,150,'Даты отметок',True)
s.line([(246.5,164),(260,164),(260,171),(246.5,171)],arrow='filled');s.text(300,167.5,'7 Рассчитать прогресс',5.2)
msg(8,2,3,184,'Подтвердить транзакцию');msg(9,3,2,198,'Подтверждено',True);msg(10,2,1,212,'Прогресс',True);msg(11,1,0,226,'Обновлённый экран',True)
s.text(220,232.5,'Рисунок В.1 – Ежедневная отметка привычки',4.94,font='Times')
# Form 1 title block, 185 x 55 mm, with standard column widths.
x,y=230,237;s.rect(x,y,185,55,lw=.7);s.line([(x+65,y),(x+65,y+55)],lw=.7);s.line([(x+65,y+15),(x+185,y+15)],lw=.7);s.line([(x+135,y+15),(x+135,y+55)],lw=.7)
for yy in [5,10,15,20,25,30,35,40,45,50]:s.line([(x,y+yy),(x+65,y+yy)],lw=.25)
for xx in [7,17,40,55]:s.line([(x+xx,y),(x+xx,y+20)],lw=.25)
for xx in [17,40,55]:s.line([(x+xx,y+20),(x+xx,y+55)],lw=.25)
for xx in [150,167]:s.line([(x+xx,y+15),(x+xx,y+35)],lw=.25)
for yy in [20,35,40]:s.line([(x+135,y+yy),(x+185,y+yy)],lw=.25)
s.text(x+125,y+7.5,'БГУИР ЛР 6-05-0611-05 16 ПД',5.0,font='Narrow',maxwidth=116)
s.text(x+100,y+34,'Ритмо\nЕжедневная отметка\nДиаграмма\nпоследовательности',5.0,font='Narrow',maxwidth=66)
for cx,label in [(3.5,'Изм.'),(12,'Лист'),(28.5,'№ докум.'),(47.5,'Подп.'),(60,'Дата')]:s.text(x+cx,y+17.5,label,3.8,font='Narrow')
for yy,role,name in [(22.5,'Разраб.','Нагорный'),(27.5,'Пров.','Куприянова'),(32.5,'Т. контр.',''),(37.5,'Реценз.',''),(42.5,'Н. контр.',''),(47.5,'Утв.','')]:s.text(x+8.5,y+yy,role,4.2,font='Narrow',maxwidth=16);s.text(x+28.5,y+yy,name,4.2,font='Narrow',maxwidth=22)
for xx,label in [(142.5,'Лит.'),(158.5,'Масса'),(176,'Масштаб')]:s.text(x+xx,y+17.5,label,4.2,font='Narrow')
s.text(x+160,y+37.5,'Лист 1   Листов 1',4.2,font='Narrow');s.text(x+160,y+47.5,'Кафедра ЭВМ\nГруппа 450501',4.8,font='Narrow',maxwidth=47)
s.save()
print('Five vector diagrams built; text widths checked.')

# Raster versions embedded into Word; the A3 deliverable remains vector PDF.
import shutil, subprocess
poppler=shutil.which('pdftoppm')
if poppler is None:
    candidate=Path(__import__('sys').executable).resolve().parents[2]/'bin/override/pdftoppm'
    if candidate.exists():poppler=str(candidate)
if not poppler:raise RuntimeError('pdftoppm is required to render PNG outputs')
for name in ['use-cases','activity-create-habit','activity-complete-today','state-habit','sequence-complete-today-a3']:
    subprocess.run([poppler,'-r','300','-singlefile','-png',str(QA/(name+'.pdf')),str(OUT/'images'/name)],check=True)
for target in ['sequence-complete-today-a3.pdf','приложение-Б.pdf']:
    shutil.copy2(QA/'sequence-complete-today-a3.pdf',OUT/'images'/target)
print('PNG/SVG/draw.io refreshed; vector A3 PDF refreshed.')
