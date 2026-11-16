const pptxgen = require("pptxgenjs");
const path = require("path");

const DOCS = path.join(__dirname, "..", "Documentos");

const THEME = {
  name: "MoedaEstudantil",
  headFontFace: "Cambria",
  bodyFontFace: "Calibri",
  colors: {
    dk1: "1C1C1C",
    lt1: "FFFFFF",
    dk2: "21295C",
    lt2: "F2F2F2",
    accent1: "F9A826",
    accent2: "065A82",
    accent3: "1C7293",
    accent4: "CADCFC",
    accent5: "97BC62",
    accent6: "6D2E46",
    hlink: "1C7293",
    folHlink: "6D2E46",
  },
};

const pres = new pptxgen();
pres.layout = "LAYOUT_WIDE";
pres.theme = { headFontFace: THEME.headFontFace, bodyFontFace: THEME.bodyFontFace };
const C = pres.SchemeColor;

// ---- Layouts ----
pres.defineSlideMaster({
  title: "TITLE",
  background: { color: "21295C" },
  objects: [
    { placeholder: { options: { name: "title", type: "title", x: 0.8, y: 2.6, w: 11.7, h: 1.3, fontSize: 40, bold: true, color: "FFFFFF", fontFace: THEME.headFontFace, align: "left" }, text: "" } },
    { placeholder: { options: { name: "subtitle", type: "body", x: 0.8, y: 3.9, w: 11.7, h: 0.8, fontSize: 18, color: "CADCFC", fontFace: THEME.bodyFontFace, align: "left" }, text: "" } },
  ],
});

pres.defineSlideMaster({
  title: "CONTENT",
  background: { color: "FFFFFF" },
  objects: [
    { placeholder: { options: { name: "title", type: "title", x: 0.6, y: 0.4, w: 12.1, h: 0.9, fontSize: 30, bold: true, color: "21295C", fontFace: THEME.headFontFace, align: "left" }, text: "" } },
    { text: { options: { x: 0.6, y: 7.05, w: 12.1, h: 0.3, fontSize: 10, color: "9A9A9A", align: "right", fontFace: THEME.bodyFontFace }, text: "Sistema de Moeda Estudantil — Projeto de Software" } },
  ],
});

pres.defineSlideMaster({
  title: "IMAGE_FULL",
  background: { color: "FFFFFF" },
  objects: [
    { placeholder: { options: { name: "title", type: "title", x: 0.6, y: 0.35, w: 12.1, h: 0.75, fontSize: 26, bold: true, color: "21295C", fontFace: THEME.headFontFace, align: "left" }, text: "" } },
  ],
});

// ---- Slide 1: Título ----
pres.addSlide({ masterName: "TITLE" }).addText(
  [{ text: "Sistema de Moeda Estudantil" }],
  { placeholder: "title" }
);
pres.slides[0].addText(
  "Reconhecimento de mérito estudantil através de uma moeda virtual\nProjeto de Software — PUC Minas",
  { placeholder: "subtitle" }
);
pres.slides[0].addShape(pres.ShapeType.ellipse, { x: 10.4, y: -1.2, w: 4.5, h: 4.5, fill: { color: "1C7293", transparency: 70 }, line: { type: "none" } });
pres.slides[0].addShape(pres.ShapeType.ellipse, { x: 11.6, y: 4.3, w: 3, h: 3, fill: { color: "F9A826", transparency: 60 }, line: { type: "none" } });

// ---- Slide 2: Visão geral ----
let s2 = pres.addSlide({ masterName: "CONTENT" });
s2.addText("Visão Geral do Sistema", { placeholder: "title" });

const atores = [
  { label: "Professores", desc: "Recebem 1.000 moedas/semestre (saldo acumulável) e distribuem aos alunos com motivo obrigatório", color: "065A82" },
  { label: "Alunos", desc: "Recebem moedas, são notificados por e-mail e trocam por vantagens com empresas parceiras", color: "1C7293" },
  { label: "Empresas Parceiras", desc: "Cadastram vantagens (foto, descrição, custo) e conferem resgates via cupom por e-mail", color: "F9A826" },
];

atores.forEach((a, i) => {
  const x = 0.6 + i * 4.1;
  s2.addShape(pres.ShapeType.roundRect, { x, y: 1.5, w: 3.8, h: 3.4, rectRadius: 0.12, fill: { color: "F2F2F2" }, line: { type: "none" }, shadow: { type: "outer", color: "000000", opacity: 0.15, blur: 6, offset: 2, angle: 90 } });
  s2.addShape(pres.ShapeType.ellipse, { x: x + 0.3, y: 1.85, w: 0.7, h: 0.7, fill: { color: a.color }, line: { type: "none" } });
  s2.addText(a.label, { x: x + 0.3, y: 2.75, w: 3.2, h: 0.5, fontSize: 18, bold: true, color: "21295C", fontFace: THEME.headFontFace, isTextBox: true, margin: 0 });
  s2.addText(a.desc, { x: x + 0.3, y: 3.3, w: 3.2, h: 1.4, fontSize: 13, color: "444444", fontFace: THEME.bodyFontFace, isTextBox: true, margin: 0, valign: "top" });
});

s2.addText("Arquitetura MVC — Spring Boot · Spring Data JPA · Banco relacional (H2)", {
  x: 0.6, y: 5.3, w: 11.9, h: 0.6, fontSize: 14, italic: true, color: "6D2E46", fontFace: THEME.bodyFontFace, isTextBox: true, align: "left",
});

// ---- Slide 3: Histórias do usuário ----
let s3 = pres.addSlide({ masterName: "CONTENT" });
s3.addText("Histórias do Usuário", { placeholder: "title" });

const stories = [
  "US01 — Cadastro de aluno (nome, e-mail, CPF, RG, endereço, instituição, curso)",
  "US02 — Autenticação de usuários (aluno, professor, empresa parceira)",
  "US03 — Envio de moedas pelo professor, com motivo obrigatório",
  "US04 — Consulta de extrato e saldo pelo professor",
  "US05 — Notificação por e-mail e extrato do aluno",
  "US06 — Cadastro de vantagens pela empresa parceira",
  "US07 — Resgate de vantagens com cupom por e-mail",
];

s3.addTable(
  stories.map((t) => [{ text: t, options: { fontSize: 14, color: "1C1C1C", fontFace: THEME.bodyFontFace, align: "left", valign: "middle" } }]),
  {
    x: 0.6, y: 1.5, w: 11.9, h: 5.3,
    colW: [11.9],
    border: { type: "none" },
    fill: { color: "F7F7F7" },
    margin: [6, 10, 6, 10],
    rowH: 0.72,
    autoPage: false,
  }
);

// ---- Slide 4: Modelagem UML ----
let s4 = pres.addSlide({ masterName: "IMAGE_FULL" });
s4.addText("Modelagem UML — Casos de Uso e Classes", { placeholder: "title" });
s4.addImage({ path: path.join(DOCS, "DiagramaCasoDeUso.png"), x: 0.6, y: 1.3, w: 5.8, h: 5.5, sizing: { type: "contain", w: 5.8, h: 5.5 } });
s4.addImage({ path: path.join(DOCS, "DiagramaDeClasse.png"), x: 6.7, y: 1.3, w: 5.9, h: 5.5, sizing: { type: "contain", w: 5.9, h: 5.5 } });

// ---- Slide 5: Componentes + ER ----
let s5 = pres.addSlide({ masterName: "IMAGE_FULL" });
s5.addText("Componentes e Modelo Entidade-Relacionamento", { placeholder: "title" });
s5.addImage({ path: path.join(DOCS, "DiagramaDeComponente.png"), x: 0.6, y: 1.3, w: 5.8, h: 5.5, sizing: { type: "contain", w: 5.8, h: 5.5 } });
s5.addImage({ path: path.join(DOCS, "Diagrama Entidade-Relacionamento.png"), x: 6.7, y: 1.3, w: 5.9, h: 5.5, sizing: { type: "contain", w: 5.9, h: 5.5 } });

// ---- Slide 6: Diagramas de sequência ----
let s6 = pres.addSlide({ masterName: "IMAGE_FULL" });
s6.addText("Fluxos Principais — Diagramas de Sequência", { placeholder: "title" });
s6.addImage({ path: path.join(DOCS, "Diagrama de Sequência — Envio de Moedas.png"), x: 0.6, y: 1.3, w: 5.8, h: 5.5, sizing: { type: "contain", w: 5.8, h: 5.5 } });
s6.addImage({ path: path.join(DOCS, "Diagrama de Sequência — Resgate de Vantagem.png"), x: 6.7, y: 1.3, w: 5.9, h: 5.5, sizing: { type: "contain", w: 5.9, h: 5.5 } });

// ---- Slide 7: Stack + status ----
let s7 = pres.addSlide({ masterName: "CONTENT" });
s7.addText("Tecnologias e Status das Entregas", { placeholder: "title" });

const stack = ["Java (JDK 17+)", "Spring Boot — Web & Data JPA", "Banco relacional H2 (ORM/Hibernate)", "Arquitetura MVC"];
s7.addText(
  stack.map((t, i) => ({ text: t, options: { bullet: true, breakLine: i < stack.length - 1, color: "1C1C1C", fontFace: THEME.bodyFontFace, fontSize: 15, paraSpaceAfter: 8 } })),
  { x: 0.6, y: 1.5, w: 5.8, h: 3.2, isTextBox: true, valign: "top" }
);
s7.addText("Stack Tecnológica", { x: 0.6, y: 1.05, w: 5.8, h: 0.4, fontSize: 16, bold: true, color: "065A82", fontFace: THEME.headFontFace, isTextBox: true });

const sprints = [
  { nome: "Sprint 1", desc: "Histórias do usuário + UML (casos de uso, classes, componentes)" },
  { nome: "Sprint 2", desc: "Modelo ER, estratégia de persistência, CRUDs iniciais" },
  { nome: "Sprint 3", desc: "CRUDs finais, regras de negócio e transações completas" },
];
sprints.forEach((sp, i) => {
  const y = 1.05 + i * 1.5;
  s7.addShape(pres.ShapeType.roundRect, { x: 6.9, y, w: 5.6, h: 1.25, rectRadius: 0.1, fill: { color: "F2F2F2" }, line: { type: "none" } });
  s7.addText(sp.nome, { x: 7.15, y: y + 0.12, w: 5.1, h: 0.35, fontSize: 14, bold: true, color: "6D2E46", fontFace: THEME.headFontFace, isTextBox: true, margin: 0 });
  s7.addText(sp.desc, { x: 7.15, y: y + 0.5, w: 5.1, h: 0.65, fontSize: 12, color: "444444", fontFace: THEME.bodyFontFace, isTextBox: true, margin: 0, valign: "top" });
});

(async () => {
  await pres.writeFile({ fileName: path.join(__dirname, "Sistema de Moeda Estudantil - Apresentacao.pptx") });
  const { applyTheme } = require(path.join(
    "C:\\Users\\VM_Claude3.0\\AppData\\Local\\Claude-Data\\claude_pzs8sxrjxfjjc\\local-agent-mode-sessions\\skills-plugin\\f4db6b0c-a64e-40e4-b993-0a06f6e96dcb\\0f6f3c80-3267-44d5-9cf8-fd5a02741f5e\\skills\\pptx\\scripts\\apply_theme.js"
  ));
  await applyTheme(path.join(__dirname, "Sistema de Moeda Estudantil - Apresentacao.pptx"), THEME);
  console.log("done");
})();
