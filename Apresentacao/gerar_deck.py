# -*- coding: utf-8 -*-
import os
from pptx import Presentation
from pptx.util import Inches, Pt, Emu
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE
from pptx.oxml.ns import qn
from PIL import Image

BASE = os.path.dirname(__file__)
DOCS = os.path.join(BASE, "..", "Documentos")
OUT = os.path.join(BASE, "Sistema de Moeda Estudantil - Apresentacao.pptx")

NAVY = RGBColor(0x21, 0x29, 0x5C)
WHITE = RGBColor(0xFF, 0xFF, 0xFF)
ICE = RGBColor(0xCA, 0xDC, 0xFC)
TEAL = RGBColor(0x1C, 0x72, 0x93)
BLUE = RGBColor(0x06, 0x5A, 0x82)
GOLD = RGBColor(0xF9, 0xA8, 0x26)
BERRY = RGBColor(0x6D, 0x2E, 0x46)
LIGHT = RGBColor(0xF2, 0xF2, 0xF2)
DARKTXT = RGBColor(0x1C, 0x1C, 0x1C)
GRAYTXT = RGBColor(0x44, 0x44, 0x44)

HEAD_FONT = "Cambria"
BODY_FONT = "Calibri"

prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.5)
BLANK = prs.slide_layouts[6]


def add_slide():
    return prs.slides.add_slide(BLANK)


def set_bg(slide, color):
    bg = slide.background
    bg.fill.solid()
    bg.fill.fore_color.rgb = color


def add_rect(slide, x, y, w, h, color, radius=False, line=False):
    shape_type = MSO_SHAPE.ROUNDED_RECTANGLE if radius else MSO_SHAPE.RECTANGLE
    shp = slide.shapes.add_shape(shape_type, Inches(x), Inches(y), Inches(w), Inches(h))
    shp.fill.solid()
    shp.fill.fore_color.rgb = color
    if line:
        shp.line.color.rgb = line
        shp.line.width = Pt(0.75)
    else:
        shp.line.fill.background()
    shp.shadow.inherit = False
    return shp


def add_circle(slide, x, y, d, color, transparency=None):
    shp = slide.shapes.add_shape(MSO_SHAPE.OVAL, Inches(x), Inches(y), Inches(d), Inches(d))
    shp.fill.solid()
    shp.fill.fore_color.rgb = color
    shp.line.fill.background()
    shp.shadow.inherit = False
    if transparency is not None:
        set_transparency(shp, transparency)
    return shp


def set_transparency(shape, pct):
    alpha = int((100 - pct) * 1000)
    sp = shape.fill.fore_color._xFill
    srgb = sp.find(qn("a:srgbClr"))
    if srgb is not None:
        a = srgb.makeelement(qn("a:alpha"), {"val": str(alpha)})
        srgb.append(a)


def add_text(slide, x, y, w, h, text, size=14, bold=False, italic=False, color=DARKTXT,
             font=BODY_FONT, align=PP_ALIGN.LEFT, anchor=MSO_ANCHOR.TOP, line_spacing=None):
    tb = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = tb.text_frame
    tf.word_wrap = True
    tf.vertical_anchor = anchor
    tf.margin_left = 0
    tf.margin_right = 0
    tf.margin_top = 0
    tf.margin_bottom = 0
    lines = text.split("\n")
    for i, line in enumerate(lines):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.alignment = align
        if line_spacing:
            p.line_spacing = line_spacing
        r = p.add_run()
        r.text = line
        r.font.size = Pt(size)
        r.font.bold = bold
        r.font.italic = italic
        r.font.color.rgb = color
        r.font.name = font
    return tb


def add_bullets(slide, x, y, w, h, items, size=14, color=DARKTXT, font=BODY_FONT, space_after=8):
    tb = slide.shapes.add_textbox(Inches(x), Inches(y), Inches(w), Inches(h))
    tf = tb.text_frame
    tf.word_wrap = True
    tf.margin_left = 0
    tf.margin_right = 0
    tf.margin_top = 0
    tf.margin_bottom = 0
    for i, item in enumerate(items):
        p = tf.paragraphs[0] if i == 0 else tf.add_paragraph()
        p.space_after = Pt(space_after)
        pPr = p._pPr
        if pPr is None:
            pPr = p.get_or_add_pPr()
        buChar = pPr.makeelement(qn("a:buChar"), {"char": "•"})
        pPr.append(buChar)
        pPr.set("marL", "228600")
        pPr.set("indent", "-228600")
        r = p.add_run()
        r.text = item
        r.font.size = Pt(size)
        r.font.color.rgb = color
        r.font.name = font
    return tb


def footer(slide):
    add_text(slide, 0.6, 7.05, 11.9, 0.3, "Sistema de Moeda Estudantil — Projeto de Software",
              size=10, color=RGBColor(0x9A, 0x9A, 0x9A), align=PP_ALIGN.RIGHT)


def contain_size(img_path, max_w, max_h):
    with Image.open(img_path) as im:
        iw, ih = im.size
    ratio = min(max_w / iw, max_h / ih)
    return iw * ratio, ih * ratio


def add_image_contain(slide, img_path, x, y, max_w, max_h):
    w, h = contain_size(img_path, max_w, max_h)
    px = x + (max_w - w) / 2
    py = y + (max_h - h) / 2
    slide.shapes.add_picture(img_path, Inches(px), Inches(py), Inches(w), Inches(h))


# ---------------- Slide 1: Título ----------------
s = add_slide()
set_bg(s, NAVY)
add_circle(s, 10.4, -1.2, 4.5, TEAL, transparency=70)
add_circle(s, 11.6, 4.3, 3, GOLD, transparency=60)
add_text(s, 0.8, 2.6, 11.0, 1.3, "Sistema de Moeda Estudantil", size=40, bold=True, color=WHITE, font=HEAD_FONT)
add_text(s, 0.8, 3.9, 11.0, 0.9,
          "Reconhecimento de mérito estudantil através de uma moeda virtual\nProjeto de Software — PUC Minas",
          size=18, color=ICE, font=BODY_FONT, line_spacing=1.3)

# ---------------- Slide 2: Visão geral ----------------
s = add_slide()
set_bg(s, WHITE)
add_text(s, 0.6, 0.4, 12.1, 0.9, "Visão Geral do Sistema", size=30, bold=True, color=NAVY, font=HEAD_FONT)

atores = [
    ("Professores", "Recebem 1.000 moedas/semestre (saldo acumulável) e distribuem aos alunos com motivo obrigatório", BLUE),
    ("Alunos", "Recebem moedas, são notificados por e-mail e trocam por vantagens com empresas parceiras", TEAL),
    ("Empresas Parceiras", "Cadastram vantagens (foto, descrição, custo) e conferem resgates via cupom por e-mail", GOLD),
]
for i, (label, desc, color) in enumerate(atores):
    x = 0.6 + i * 4.1
    add_rect(s, x, 1.5, 3.8, 3.4, LIGHT, radius=True)
    add_circle(s, x + 0.3, 1.85, 0.7, color)
    add_text(s, x + 0.3, 2.75, 3.2, 0.5, label, size=18, bold=True, color=NAVY, font=HEAD_FONT)
    add_text(s, x + 0.3, 3.3, 3.2, 1.4, desc, size=13, color=GRAYTXT, font=BODY_FONT)

add_text(s, 0.6, 5.3, 11.9, 0.5,
          "Arquitetura MVC — Spring Boot · Spring Data JPA · Banco relacional (H2)",
          size=14, italic=True, color=BERRY, font=BODY_FONT)
footer(s)

# ---------------- Slide 3: Histórias do usuário ----------------
s = add_slide()
set_bg(s, WHITE)
add_text(s, 0.6, 0.4, 12.1, 0.9, "Histórias do Usuário", size=30, bold=True, color=NAVY, font=HEAD_FONT)

stories = [
    "US01 — Cadastro de aluno (nome, e-mail, CPF, RG, endereço, instituição, curso)",
    "US02 — Autenticação de usuários (aluno, professor, empresa parceira)",
    "US03 — Envio de moedas pelo professor, com motivo obrigatório",
    "US04 — Consulta de extrato e saldo pelo professor",
    "US05 — Notificação por e-mail e extrato do aluno",
    "US06 — Cadastro de vantagens pela empresa parceira",
    "US07 — Resgate de vantagens com cupom por e-mail",
]
y = 1.5
for i, txt in enumerate(stories):
    add_rect(s, 0.6, y, 11.9, 0.68, LIGHT if i % 2 == 0 else RGBColor(0xFA, 0xFA, 0xFA))
    add_text(s, 0.9, y, 11.3, 0.68, txt, size=14, color=DARKTXT, font=BODY_FONT, anchor=MSO_ANCHOR.MIDDLE)
    y += 0.73
footer(s)

# ---------------- Slide 4: UML Casos de Uso + Classes ----------------
s = add_slide()
set_bg(s, WHITE)
add_text(s, 0.6, 0.35, 12.1, 0.75, "Modelagem UML — Casos de Uso e Classes", size=26, bold=True, color=NAVY, font=HEAD_FONT)
add_image_contain(s, os.path.join(DOCS, "DiagramaCasoDeUso.png"), 0.6, 1.3, 5.8, 5.5)
add_image_contain(s, os.path.join(DOCS, "DiagramaDeClasse.png"), 6.7, 1.3, 5.9, 5.5)

# ---------------- Slide 5: Componentes + ER ----------------
s = add_slide()
set_bg(s, WHITE)
add_text(s, 0.6, 0.35, 12.1, 0.75, "Componentes e Modelo Entidade-Relacionamento", size=26, bold=True, color=NAVY, font=HEAD_FONT)
add_image_contain(s, os.path.join(DOCS, "DiagramaDeComponente.png"), 0.6, 1.3, 5.8, 5.5)
add_image_contain(s, os.path.join(DOCS, "Diagrama Entidade-Relacionamento.png"), 6.7, 1.3, 5.9, 5.5)

# ---------------- Slide 6: Sequência ----------------
s = add_slide()
set_bg(s, WHITE)
add_text(s, 0.6, 0.35, 12.1, 0.75, "Fluxos Principais — Diagramas de Sequência", size=26, bold=True, color=NAVY, font=HEAD_FONT)
add_image_contain(s, os.path.join(DOCS, "Diagrama de Sequência — Envio de Moedas.png"), 0.6, 1.3, 5.8, 5.5)
add_image_contain(s, os.path.join(DOCS, "Diagrama de Sequência — Resgate de Vantagem.png"), 6.7, 1.3, 5.9, 5.5)

# ---------------- Slide 7: Stack + status ----------------
s = add_slide()
set_bg(s, WHITE)
add_text(s, 0.6, 0.4, 12.1, 0.9, "Tecnologias e Status das Entregas", size=30, bold=True, color=NAVY, font=HEAD_FONT)

add_text(s, 0.6, 1.05, 5.8, 0.4, "Stack Tecnológica", size=16, bold=True, color=BLUE, font=HEAD_FONT)
stack = ["Java (JDK 17+)", "Spring Boot — Web & Data JPA", "Banco relacional H2 (ORM/Hibernate)", "Arquitetura MVC"]
add_bullets(s, 0.6, 1.55, 5.8, 3.0, stack, size=15, color=DARKTXT, font=BODY_FONT, space_after=10)

sprints = [
    ("Sprint 1", "Histórias do usuário + UML (casos de uso, classes, componentes)"),
    ("Sprint 2", "Modelo ER, estratégia de persistência, CRUDs iniciais"),
    ("Sprint 3", "CRUDs finais, regras de negócio e transações completas"),
]
y = 1.05
for nome, desc in sprints:
    add_rect(s, 6.9, y, 5.6, 1.25, LIGHT, radius=True)
    add_text(s, 7.15, y + 0.12, 5.1, 0.35, nome, size=14, bold=True, color=BERRY, font=HEAD_FONT)
    add_text(s, 7.15, y + 0.5, 5.1, 0.65, desc, size=12, color=GRAYTXT, font=BODY_FONT)
    y += 1.5
footer(s)

prs.save(OUT)
print("saved:", OUT)
