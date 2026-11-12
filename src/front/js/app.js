const API = '/api';
let session = JSON.parse(localStorage.getItem('besiktas_session') || 'null');
let loginRole = 'ALUNO';

function api(path, options) {
  options = options || {};
  options.headers = Object.assign({'Content-Type': 'application/json'}, options.headers || {});
  if (session && session.token) {
    options.headers.Authorization = 'Bearer ' + session.token;
  }
  return fetch(API + path, options).then(async res => {
    const isJson = (res.headers.get('content-type') || '').includes('application/json');
    const body = isJson ? await res.json().catch(() => null) : await res.text();
    if (!res.ok) {
      const msg = (body && body.erro) ? body.erro : (typeof body === 'string' && body ? body : 'Ocorreu um erro inesperado.');
      throw new Error(msg);
    }
    return body;
  });
}

function fmtMoeda(n) { return Number(n || 0).toLocaleString('pt-BR'); }
function fmtData(d) { return d ? new Date(d).toLocaleDateString('pt-BR') : ''; }

const NAV_BY_STATE = {
  guest: [
    {id:'login', label:'Login'},
    {id:'cadastro-aluno', label:'Criar cadastro'},
    {id:'cadastro-empresa', label:'Cadastro empresa'},
  ],
  ALUNO: [
    {id:'aluno', label:'Início'},
    {id:'extrato', label:'Extrato completo'},
    {id:'perfil', label:'Meu perfil'},
  ],
  PROFESSOR: [
    {id:'professor', label:'Início'},
  ],
  EMPRESA: [
    {id:'empresa', label:'Início'},
    {id:'vantagens-gerenciar', label:'Gerenciar vantagens'},
  ],
};

function renderNav() {
  const tabsEl = document.getElementById('nav-tabs');
  const items = session ? NAV_BY_STATE[session.tipo] : NAV_BY_STATE.guest;
  tabsEl.innerHTML = '';
  items.forEach(item => {
    const btn = document.createElement('button');
    btn.textContent = item.label;
    btn.dataset.tab = item.id;
    btn.addEventListener('click', () => showScreen(item.id));
    tabsEl.appendChild(btn);
  });
  document.getElementById('logout-btn').style.display = session ? 'inline-block' : 'none';

  const isAluno = session && session.tipo === 'ALUNO';
  document.getElementById('notif-wrap').style.display = isAluno ? 'flex' : 'none';
  if (isAluno) loadNotificacoesBadge();
}

// ---------- NOTIFICAÇÕES ----------
function notifIcone(mensagem) {
  if (mensagem.indexOf('Resgate confirmado') === 0) {
    return '<div class="notif-icon" style="background:#fdf0cf;">🎟️</div>';
  }
  return '<div class="notif-icon" style="background:var(--purple-light);"><svg class="coin" width="18" height="18"><use href="#coin-icon"/></svg></div>';
}

function loadNotificacoesBadge() {
  if (!session || session.tipo !== 'ALUNO') return;
  api('/notificacoes/aluno/' + session.usuario.cpf + '/nao-lidas').then(data => {
    const badge = document.getElementById('notif-badge');
    if (data.naoLidas > 0) {
      badge.textContent = data.naoLidas > 9 ? '9+' : data.naoLidas;
      badge.style.display = 'flex';
    } else {
      badge.style.display = 'none';
    }
  });
}

function renderNotifPanel() {
  api('/notificacoes/aluno/' + session.usuario.cpf).then(lista => {
    const listEl = document.getElementById('notif-list');
    listEl.innerHTML = lista.length ? lista.map(n => `
      <div class="notif-item ${n.lida ? 'lida' : ''}" ${n.lida ? '' : `onclick="marcarNotificacaoComoLida(${n.id})"`}>
        ${notifIcone(n.mensagem)}
        <div class="notif-text" style="flex:1;">
          <p>${n.mensagem}</p>
          <div class="notif-data">${fmtData(n.dataCriacao)}</div>
        </div>
        ${n.lida ? '' : '<div class="notif-dot"></div>'}
      </div>`).join('') : '<div class="notif-empty">Nenhuma notificação por aqui.</div>';
  });
}

function toggleNotifPanel() {
  const panel = document.getElementById('notif-panel');
  const abrindo = !panel.classList.contains('open');
  panel.classList.toggle('open', abrindo);
  if (abrindo) renderNotifPanel();
}

function marcarNotificacaoComoLida(id) {
  api('/notificacoes/' + id + '/ler', { method: 'PATCH' }).then(() => {
    renderNotifPanel();
    loadNotificacoesBadge();
  });
}

function marcarTodasComoLidas() {
  api('/notificacoes/aluno/' + session.usuario.cpf).then(lista => {
    const naoLidas = lista.filter(n => !n.lida);
    Promise.all(naoLidas.map(n => api('/notificacoes/' + n.id + '/ler', { method: 'PATCH' }))).then(() => {
      renderNotifPanel();
      loadNotificacoesBadge();
    });
  });
}

function setActiveTab(id) {
  document.querySelectorAll('#nav-tabs button').forEach(b => b.classList.toggle('active', b.dataset.tab === id));
}

const SCREEN_LOADERS = {
  aluno: loadAlunoScreen,
  extrato: loadExtratoScreen,
  perfil: loadPerfilScreen,
  professor: loadProfessorScreen,
  empresa: loadEmpresaScreen,
  'vantagens-gerenciar': loadVantagensGerenciarScreen,
};

const SCREEN_REQUIRES_ROLE = {
  aluno: 'ALUNO', extrato: 'ALUNO', perfil: 'ALUNO',
  professor: 'PROFESSOR',
  empresa: 'EMPRESA', 'vantagens-gerenciar': 'EMPRESA',
};

function showScreen(id) {
  const requiredRole = SCREEN_REQUIRES_ROLE[id];
  if (requiredRole && (!session || session.tipo !== requiredRole)) {
    id = 'login';
  }
  document.querySelectorAll('.screen').forEach(s => s.classList.remove('active'));
  const target = document.getElementById(id);
  if (!target) return;
  target.classList.add('active');
  setActiveTab(id);
  if (SCREEN_LOADERS[id]) SCREEN_LOADERS[id]();
}

function routeAfterLogin() {
  renderNav();
  if (session.tipo === 'ALUNO') showScreen('aluno');
  else if (session.tipo === 'PROFESSOR') showScreen('professor');
  else if (session.tipo === 'EMPRESA') showScreen('empresa');
}

function logout() {
  session = null;
  localStorage.removeItem('besiktas_session');
  renderNav();
  showScreen('login');
}

function saveSession() {
  localStorage.setItem('besiktas_session', JSON.stringify(session));
}

// ---------- CADASTRO ALUNO ----------
function loadInstituicoesSelect() {
  const select = document.getElementById('ca-instituicao');
  if (select.dataset.loaded) return;
  api('/instituicoes').then(lista => {
    select.innerHTML = lista.map(i => `<option value="${i.id}">${i.nome}</option>`).join('');
    select.dataset.loaded = '1';
  }).catch(() => {
    select.innerHTML = '<option value="">Não foi possível carregar as instituições</option>';
  });
}

// ---------- ALUNO ----------
function renderTransacaoRow(t, perspectiva) {
  const isResgate = !!t.vantagem;
  const sinalIn = perspectiva === 'recebimento';
  const titulo = isResgate
    ? ('Resgate: ' + (t.vantagem ? t.vantagem.descricao : ''))
    : (t.professor ? ('Prof. ' + t.professor.nome) : (t.aluno ? t.aluno.nome : 'Transação'));
  const sub = isResgate
    ? ('Cupom ' + (t.codigoCupom || '') + ' · ' + fmtData(t.data))
    : ('"' + (t.motivo || '') + '" · ' + fmtData(t.data));
  return `
    <div class="row">
      <div class="ticon ${sinalIn ? 'in' : 'out'}">${sinalIn ? '+' : '−'}</div>
      <div class="trow-main">
        <div class="trow-title">${titulo}</div>
        <div class="trow-sub">${sub}</div>
      </div>
      <div class="trow-amt ${sinalIn ? 'in' : 'out'}">${sinalIn ? '+' : '−'}${fmtMoeda(t.quantidade)}</div>
    </div>`;
}

let alunoTransacoesCache = [];

function loadAlunoScreen() {
  const cpf = session.usuario.cpf;
  document.getElementById('aluno-toast').style.display = 'none';

  Promise.all([
    api('/alunos/' + cpf),
    api('/transacoes/aluno/' + cpf),
    api('/vantagens'),
  ]).then(([aluno, transacoes, vantagens]) => {
    session.usuario = aluno;
    saveSession();
    alunoTransacoesCache = transacoes;

    document.getElementById('aluno-nome').innerHTML = aluno.nome + ' <span class="level-badge" id="aluno-nivel"></span>';
    document.getElementById('aluno-saldo').textContent = fmtMoeda(aluno.saldoMoedas);

    const recebimentos = transacoes.filter(t => !t.vantagem);
    const nivel = Math.max(1, Math.floor(recebimentos.length / 2) + 1);
    document.getElementById('aluno-nivel').textContent = 'Nível ' + nivel;
    document.getElementById('aluno-xp').style.width = Math.min(100, (recebimentos.length % 5) * 20 + 10) + '%';
    document.getElementById('aluno-streak').textContent = recebimentos.length
      ? ('🔥 ' + recebimentos.length + ' reconhecimento(s) recebido(s) no total')
      : 'Ainda sem reconhecimentos recebidos.';

    const ultimaRecebida = recebimentos[0];
    if (ultimaRecebida) {
      document.getElementById('aluno-toast-text').textContent = '+' + fmtMoeda(ultimaRecebida.quantidade) + ' moedas recebidas de ' + (ultimaRecebida.professor ? ultimaRecebida.professor.nome : 'um professor');
      document.getElementById('aluno-toast').style.display = 'flex';
    }

    const recebidasEl = document.getElementById('aluno-recebidas');
    recebidasEl.innerHTML = recebimentos.length
      ? recebimentos.slice(0, 5).map(t => renderTransacaoRow(t, 'recebimento')).join('')
      : '<div class="empty-state">Você ainda não recebeu moedas.</div>';

    const resgates = transacoes.filter(t => t.vantagem);
    const cupomEl = document.getElementById('aluno-cupom');
    if (resgates.length) {
      const ultimo = resgates[0];
      cupomEl.innerHTML = `
        <div class="coupon">
          <div>
            <div style="font-size:12px; color:var(--ink-soft);">${ultimo.vantagem.descricao}</div>
            <code>${ultimo.codigoCupom}</code>
          </div>
          <span class="badge gold">resgatado</span>
        </div>`;
    } else {
      cupomEl.innerHTML = '<div class="empty-state">Você ainda não resgatou nenhuma vantagem.</div>';
    }

    const lojaEl = document.getElementById('aluno-loja');
    const vantagensOrdenadas = [...vantagens].sort((a, b) => a.custoMoedas - b.custoMoedas);
    lojaEl.innerHTML = vantagensOrdenadas.map(v => {
      const podeResgatar = aluno.saldoMoedas >= v.custoMoedas;
      return `
        <div class="reward-card">
          <div class="reward-img">foto da vantagem</div>
          <div class="reward-body">
            <div class="reward-title">${v.descricao}</div>
            <div class="reward-partner">${v.empresaParceira ? v.empresaParceira.nome : ''}</div>
            <div class="reward-foot">
              <div class="price"><svg class="coin" width="16" height="16"><use href="#coin-icon"/></svg> ${fmtMoeda(v.custoMoedas)}</div>
              <button class="cta ${podeResgatar ? '' : 'locked'}" ${podeResgatar ? `onclick="resgatarVantagem(${v.id})"` : 'disabled'}>
                ${podeResgatar ? 'Resgatar' : 'Saldo insuficiente'}
              </button>
            </div>
          </div>
        </div>`;
    }).join('') || '<div class="empty-state">Nenhuma vantagem publicada ainda.</div>';
  }).catch(err => {
    document.getElementById('aluno-recebidas').innerHTML = '<div class="empty-state">Erro ao carregar: ' + err.message + '</div>';
  });
}

function resgatarVantagem(vantagemId) {
  const cpf = session.usuario.cpf;
  api('/transacoes/resgatar?alunoCpf=' + encodeURIComponent(cpf) + '&vantagemId=' + vantagemId, { method: 'POST' })
    .then(transacao => {
      document.getElementById('confirmacao-descricao').textContent =
        transacao.vantagem.descricao + (transacao.vantagem.empresaParceira ? (' · ' + transacao.vantagem.empresaParceira.nome) : '');
      document.getElementById('confirmacao-codigo').textContent = transacao.codigoCupom;
      showScreen('confirmacao');
    })
    .catch(err => alert(err.message));
}

// ---------- EXTRATO COMPLETO ----------
function loadExtratoScreen() {
  const cpf = session.usuario.cpf;
  Promise.all([api('/alunos/' + cpf), api('/transacoes/aluno/' + cpf)]).then(([aluno, transacoes]) => {
    document.getElementById('extrato-resumo').textContent = aluno.nome + ' · saldo atual ' + fmtMoeda(aluno.saldoMoedas);
    alunoTransacoesCache = transacoes;
    renderExtrato();
  });
}

function renderExtrato() {
  const tipo = document.getElementById('extrato-tipo').value;
  let lista = alunoTransacoesCache;
  if (tipo === 'recebimentos') lista = lista.filter(t => !t.vantagem);
  if (tipo === 'resgates') lista = lista.filter(t => !!t.vantagem);
  const el = document.getElementById('extrato-lista');
  el.innerHTML = lista.length
    ? lista.map(t => renderTransacaoRow(t, t.vantagem ? 'resgate' : 'recebimento')).join('')
    : '<div class="empty-state">Nenhuma transação encontrada para este filtro.</div>';
}

// ---------- PERFIL ----------
function loadPerfilScreen() {
  const cpf = session.usuario.cpf;
  document.getElementById('perfil-error').style.display = 'none';
  document.getElementById('perfil-sucesso').style.display = 'none';
  api('/alunos/' + cpf).then(aluno => {
    session.usuario = aluno;
    saveSession();
    document.getElementById('perfil-nome-topo').textContent = aluno.nome;
    document.getElementById('perfil-saldo').textContent = fmtMoeda(aluno.saldoMoedas);
    document.getElementById('perfil-nome').value = aluno.nome || '';
    document.getElementById('perfil-cpf').value = aluno.cpf || '';
    document.getElementById('perfil-email').value = aluno.email || '';
    document.getElementById('perfil-curso').value = aluno.curso || '';
    document.getElementById('perfil-endereco').value = aluno.endereco || '';
  });
}

// ---------- PROFESSOR ----------
function loadProfessorScreen() {
  const cpf = session.usuario.cpf;
  document.getElementById('enviar-error').style.display = 'none';
  document.getElementById('enviar-sucesso').style.display = 'none';

  Promise.all([api('/professores/' + cpf), api('/transacoes/professor/' + cpf)]).then(([professor, transacoes]) => {
    session.usuario = professor;
    saveSession();
    document.getElementById('professor-depto').textContent = professor.departamento || 'Departamento não informado';
    document.getElementById('professor-nome').innerHTML = 'Prof. ' + professor.nome + ' <span class="level-badge">Mentor</span>';
    document.getElementById('professor-saldo').textContent = fmtMoeda(professor.saldoMoedas);
    const usadas = Math.max(0, 1000 - professor.saldoMoedas);
    document.getElementById('professor-xp').style.width = Math.min(100, (usadas / 1000) * 100) + '%';
    document.getElementById('professor-xp-label').textContent = fmtMoeda(usadas) + ' moedas usadas de 1.000 neste semestre';

    const el = document.getElementById('professor-extrato');
    el.innerHTML = transacoes.length
      ? transacoes.map(t => `
        <div class="row">
          <div class="ticon out">−</div>
          <div class="trow-main">
            <div class="trow-title">${t.aluno ? t.aluno.nome : ''}</div>
            <div class="trow-sub">"${t.motivo || ''}" · ${fmtData(t.data)}</div>
          </div>
          <div class="trow-amt out">−${fmtMoeda(t.quantidade)}</div>
        </div>`).join('')
      : '<div class="empty-state">Nenhuma moeda enviada ainda.</div>';
  });
}

// ---------- EMPRESA ----------
function loadEmpresaScreen() {
  const cnpj = session.usuario.cnpj;
  document.getElementById('vantagem-error').style.display = 'none';
  document.getElementById('vantagem-sucesso').style.display = 'none';

  Promise.all([api('/empresas/' + cnpj), api('/transacoes/empresa/' + cnpj)]).then(([empresa, cupons]) => {
    session.usuario = empresa;
    saveSession();
    document.getElementById('empresa-nome').textContent = empresa.nome;
    document.getElementById('empresa-resgates').textContent = fmtMoeda(cupons.length);

    const el = document.getElementById('empresa-cupons');
    el.innerHTML = cupons.length
      ? cupons.map(t => `
        <div class="row">
          <div class="ticon in"><svg class="coin" width="20" height="20"><use href="#coin-icon"/></svg></div>
          <div class="trow-main">
            <div class="trow-title">${t.aluno ? t.aluno.nome : ''}</div>
            <div class="trow-sub">${t.vantagem ? t.vantagem.descricao : ''} · ${fmtData(t.data)}</div>
          </div>
          <code style="font-weight:800;">${t.codigoCupom || ''}</code>
        </div>`).join('')
      : '<div class="empty-state">Nenhum resgate registrado ainda.</div>';
  });
}

// ---------- GERENCIAR VANTAGENS ----------
let vantagensGerenciarCache = [];

function loadVantagensGerenciarScreen() {
  const cnpj = session.usuario.cnpj;
  api('/vantagens?empresaCnpj=' + encodeURIComponent(cnpj)).then(lista => {
    vantagensGerenciarCache = lista;
    document.getElementById('vantagens-resumo').textContent = lista.length + ' vantagem(ns) ativa(s)';
    const el = document.getElementById('vantagens-lista');
    el.innerHTML = lista.length ? lista.map(v => `
      <div class="reward-card">
        <div class="reward-img">foto da vantagem</div>
        <div class="reward-body">
          <div class="reward-title">${v.descricao}</div>
          <div class="reward-partner">Custo atual: ${fmtMoeda(v.custoMoedas)} moedas</div>
          <div class="reward-foot">
            <div class="price"><svg class="coin" width="16" height="16"><use href="#coin-icon"/></svg> ${fmtMoeda(v.custoMoedas)}</div>
            <div style="display:flex; gap:6px;">
              <button class="cta ghost" data-edit-id="${v.id}">Editar</button>
              <button class="cta" style="background:var(--danger);" data-delete-id="${v.id}">Excluir</button>
            </div>
          </div>
        </div>
      </div>
    `).join('') : '<div class="empty-state">Você ainda não publicou nenhuma vantagem.</div>';

    el.querySelectorAll('[data-edit-id]').forEach(btn => {
      btn.addEventListener('click', () => editarVantagem(Number(btn.dataset.editId)));
    });
    el.querySelectorAll('[data-delete-id]').forEach(btn => {
      btn.addEventListener('click', () => excluirVantagem(Number(btn.dataset.deleteId)));
    });
  });
}

function editarVantagem(id) {
  const atual = vantagensGerenciarCache.find(v => v.id === id);
  if (!atual) return;
  const descricao = prompt('Nova descrição da vantagem:', atual.descricao);
  if (descricao === null) return;
  const custoStr = prompt('Novo custo em moedas:', atual.custoMoedas);
  if (custoStr === null) return;
  const custoMoedas = Number(custoStr);
  if (!descricao.trim() || !custoMoedas || custoMoedas <= 0) {
    alert('Descrição e custo válidos são obrigatórios.');
    return;
  }
  api('/vantagens/' + id, { method: 'PUT', body: JSON.stringify({ descricao, custoMoedas }) })
    .then(() => loadVantagensGerenciarScreen())
    .catch(err => alert(err.message));
}

function excluirVantagem(id) {
  if (!confirm('Tem certeza que deseja excluir esta vantagem?')) return;
  api('/vantagens/' + id, { method: 'DELETE' })
    .then(() => loadVantagensGerenciarScreen())
    .catch(err => alert(err.message));
}

// ---------- LISTENERS QUE DEPENDEM DAS VIEWS CARREGADAS ----------
function attachEventListeners() {
  document.getElementById('notif-bell').addEventListener('click', toggleNotifPanel);
  document.querySelector('.notif-panel-header a').addEventListener('click', marcarTodasComoLidas);
  document.getElementById('logout-btn').addEventListener('click', logout);

  document.addEventListener('click', e => {
    const wrap = document.getElementById('notif-wrap');
    if (wrap && !wrap.contains(e.target)) {
      document.getElementById('notif-panel').classList.remove('open');
    }
  });

  document.getElementById('login-roles').addEventListener('click', e => {
    const btn = e.target.closest('button');
    if (!btn) return;
    loginRole = btn.dataset.role;
    document.querySelectorAll('#login-roles button').forEach(b => b.classList.toggle('active', b === btn));
  });

  document.getElementById('login-submit').addEventListener('click', () => {
    const email = document.getElementById('login-email').value.trim();
    const senha = document.getElementById('login-senha').value;
    const errorEl = document.getElementById('login-error');
    errorEl.style.display = 'none';
    if (!email || !senha) {
      errorEl.textContent = 'Preencha e-mail e senha.';
      errorEl.style.display = 'block';
      return;
    }
    api('/auth/login', { method: 'POST', body: JSON.stringify({ email, senha, tipo: loginRole }) })
      .then(data => {
        session = { tipo: data.tipo, usuario: data.usuario, token: data.token };
        saveSession();
        routeAfterLogin();
      })
      .catch(err => {
        errorEl.textContent = err.message;
        errorEl.style.display = 'block';
      });
  });

  document.getElementById('cadastro-aluno-submit').addEventListener('click', () => {
    const errorEl = document.getElementById('cadastro-aluno-error');
    errorEl.style.display = 'none';
    const payload = {
      nome: document.getElementById('ca-nome').value.trim(),
      email: document.getElementById('ca-email').value.trim(),
      cpf: document.getElementById('ca-cpf').value.trim(),
      rg: document.getElementById('ca-rg').value.trim(),
      senha: document.getElementById('ca-senha').value,
      curso: document.getElementById('ca-curso').value.trim(),
      endereco: document.getElementById('ca-endereco').value.trim(),
      instituicao: { id: Number(document.getElementById('ca-instituicao').value) },
    };
    if (!payload.nome || !payload.email || !payload.cpf || !payload.senha) {
      errorEl.textContent = 'Preencha nome, e-mail, CPF e senha.';
      errorEl.style.display = 'block';
      return;
    }
    api('/alunos', { method: 'POST', body: JSON.stringify(payload) })
      .then(() => api('/auth/login', { method: 'POST', body: JSON.stringify({ email: payload.email, senha: payload.senha, tipo: 'ALUNO' }) }))
      .then(data => {
        session = { tipo: data.tipo, usuario: data.usuario, token: data.token };
        saveSession();
        routeAfterLogin();
      })
      .catch(err => {
        errorEl.textContent = err.message;
        errorEl.style.display = 'block';
      });
  });

  document.getElementById('cadastro-empresa-submit').addEventListener('click', () => {
    const errorEl = document.getElementById('cadastro-empresa-error');
    errorEl.style.display = 'none';
    const payload = {
      nome: document.getElementById('ce-nome').value.trim(),
      cnpj: document.getElementById('ce-cnpj').value.trim(),
      email: document.getElementById('ce-email').value.trim(),
      senha: document.getElementById('ce-senha').value,
    };
    if (!payload.nome || !payload.cnpj || !payload.email || !payload.senha) {
      errorEl.textContent = 'Preencha todos os campos.';
      errorEl.style.display = 'block';
      return;
    }
    api('/empresas', { method: 'POST', body: JSON.stringify(payload) })
      .then(() => api('/auth/login', { method: 'POST', body: JSON.stringify({ email: payload.email, senha: payload.senha, tipo: 'EMPRESA' }) }))
      .then(data => {
        session = { tipo: data.tipo, usuario: data.usuario, token: data.token };
        saveSession();
        routeAfterLogin();
      })
      .catch(err => {
        errorEl.textContent = err.message;
        errorEl.style.display = 'block';
      });
  });

  document.getElementById('perfil-submit').addEventListener('click', () => {
    const cpf = session.usuario.cpf;
    const errorEl = document.getElementById('perfil-error');
    const sucessoEl = document.getElementById('perfil-sucesso');
    errorEl.style.display = 'none';
    sucessoEl.style.display = 'none';
    const payload = {
      nome: document.getElementById('perfil-nome').value.trim(),
      email: document.getElementById('perfil-email').value.trim(),
      curso: document.getElementById('perfil-curso').value.trim(),
      endereco: document.getElementById('perfil-endereco').value.trim(),
    };
    api('/alunos/' + cpf, { method: 'PUT', body: JSON.stringify(payload) })
      .then(aluno => {
        session.usuario = aluno;
        saveSession();
        sucessoEl.textContent = 'Dados atualizados com sucesso.';
        sucessoEl.style.display = 'block';
        document.getElementById('perfil-nome-topo').textContent = aluno.nome;
      })
      .catch(err => {
        errorEl.textContent = err.message;
        errorEl.style.display = 'block';
      });
  });

  document.getElementById('enviar-submit').addEventListener('click', () => {
    const professorCpf = session.usuario.cpf;
    const alunoCpf = document.getElementById('enviar-cpf').value.trim();
    const quantidade = Number(document.getElementById('enviar-quantidade').value);
    const motivo = document.getElementById('enviar-motivo').value.trim();
    const errorEl = document.getElementById('enviar-error');
    const sucessoEl = document.getElementById('enviar-sucesso');
    errorEl.style.display = 'none';
    sucessoEl.style.display = 'none';

    if (!alunoCpf || !quantidade || quantidade <= 0 || !motivo) {
      errorEl.textContent = 'Preencha o CPF do aluno, uma quantidade válida e o motivo.';
      errorEl.style.display = 'block';
      return;
    }

    const params = new URLSearchParams({ professorCpf, alunoCpf, quantidade, motivo });
    api('/transacoes/enviar?' + params.toString(), { method: 'POST' })
      .then(() => {
        sucessoEl.textContent = 'Moedas enviadas com sucesso!';
        sucessoEl.style.display = 'block';
        document.getElementById('enviar-cpf').value = '';
        document.getElementById('enviar-quantidade').value = '';
        document.getElementById('enviar-motivo').value = '';
        loadProfessorScreen();
      })
      .catch(err => {
        errorEl.textContent = err.message;
        errorEl.style.display = 'block';
      });
  });

  document.getElementById('vantagem-submit').addEventListener('click', () => {
    const cnpj = session.usuario.cnpj;
    const errorEl = document.getElementById('vantagem-error');
    const sucessoEl = document.getElementById('vantagem-sucesso');
    errorEl.style.display = 'none';
    sucessoEl.style.display = 'none';
    const descricao = document.getElementById('vantagem-descricao').value.trim();
    const custoMoedas = Number(document.getElementById('vantagem-custo').value);
    if (!descricao || !custoMoedas || custoMoedas <= 0) {
      errorEl.textContent = 'Preencha a descrição e um custo válido em moedas.';
      errorEl.style.display = 'block';
      return;
    }
    api('/vantagens', { method: 'POST', body: JSON.stringify({ descricao, custoMoedas, empresaParceira: { cnpj } }) })
      .then(() => {
        sucessoEl.textContent = 'Vantagem publicada com sucesso!';
        sucessoEl.style.display = 'block';
        document.getElementById('vantagem-descricao').value = '';
        document.getElementById('vantagem-custo').value = '';
      })
      .catch(err => {
        errorEl.textContent = err.message;
        errorEl.style.display = 'block';
      });
  });
}

// ---------- BOOTSTRAP ----------
const VIEWS = [
  'login', 'cadastro-aluno', 'aluno', 'extrato', 'perfil',
  'professor', 'empresa', 'cadastro-empresa', 'vantagens-gerenciar', 'confirmacao',
];

function loadViews() {
  return Promise.all(VIEWS.map(name => fetch('views/' + name + '.html').then(r => r.text())))
    .then(htmls => {
      document.getElementById('app-main').innerHTML = htmls.join('\n');
    });
}

loadViews().then(() => {
  attachEventListeners();
  renderNav();
  loadInstituicoesSelect();
  if (session && NAV_BY_STATE[session.tipo]) {
    routeAfterLogin();
  } else {
    showScreen('login');
  }
});
