const $ = (seletor) => document.querySelector(seletor);
const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const pagamentos = { PIX: 'Pix', CARTAO: 'Cartão', DINHEIRO: 'Dinheiro' };

const cadastros = {
    livros: {
        titulo: 'Livros',
        singular: 'livro',
        novo: 'Novo livro',
        campos: [
            { nome: 'titulo', rotulo: 'Título', maxLength: 150 },
            { nome: 'autor', rotulo: 'Autor', maxLength: 100 },
            { nome: 'categoria', rotulo: 'Categoria', maxLength: 60 },
            { nome: 'isbn', rotulo: 'ISBN', pattern: '[0-9]{13}', maxLength: 13, inputMode: 'numeric' },
            { nome: 'preco', rotulo: 'Preço', type: 'number', min: '0.01', max: '99999999.99', step: '0.01' }
        ],
        colunas: ['Título', 'Autor', 'Categoria', 'ISBN', 'Preço'],
        linha: (livro) => [livro.titulo, livro.autor, livro.categoria, livro.isbn, moeda.format(livro.preco)]
    },
    vendas: {
        titulo: 'Vendas',
        singular: 'venda',
        novo: 'Nova venda',
        campos: [
            { nome: 'livroId', rotulo: 'Livro', opcoes: () => livros.map((livro) => [livro.id, livro.titulo]) },
            { nome: 'cliente', rotulo: 'Cliente', maxLength: 100 },
            { nome: 'quantidade', rotulo: 'Quantidade', type: 'number', min: '1', max: '2147483647', step: '1' },
            { nome: 'dataVenda', rotulo: 'Data da venda', type: 'date' },
            { nome: 'pagamento', rotulo: 'Pagamento', opcoes: () => Object.entries(pagamentos) }
        ],
        colunas: ['Livro', 'Cliente', 'Qtd.', 'Data', 'Pagamento', 'Total'],
        linha: (venda) => [
            livros.find((livro) => livro.id === venda.livroId)?.titulo ?? venda.livroId,
            venda.cliente, venda.quantidade, venda.dataVenda.split('-').reverse().join('/'),
            pagamentos[venda.pagamento], moeda.format(venda.total)
        ]
    },
    usuarios: {
        titulo: 'Usuários',
        singular: 'usuário',
        novo: 'Novo usuário',
        campos: [
            { nome: 'nome', rotulo: 'Nome', maxLength: 100 },
            { nome: 'email', rotulo: 'E-mail', type: 'email', maxLength: 150 },
            { nome: 'senha', rotulo: 'Senha', type: 'password', minLength: 8, maxLength: 72, autocomplete: 'new-password' }
        ],
        colunas: ['Nome', 'E-mail'],
        linha: (usuario) => [usuario.nome, usuario.email]
    }
};

let entidade = 'livros';
let livros = [];
let registroEmEdicao;
let carregamento = 0;

async function api(caminho, { method = 'GET', dados } = {}) {
    const headers = {};
    let body;
    if (method !== 'GET') {
        const csrf = await fetch('/api/csrf').then((resposta) => resposta.json());
        headers[csrf.headerName] = csrf.token;
    }
    if (dados instanceof URLSearchParams) {
        body = dados;
    } else if (dados) {
        headers['Content-Type'] = 'application/json';
        body = JSON.stringify(dados);
    }

    const resposta = await fetch('/api' + caminho, { method, headers, body });
    if (resposta.status === 401) {
        exibirLogin();
        throw new Error(caminho === '/login' ? 'E-mail ou senha inválidos.' : 'Entre para continuar.');
    }
    if (!resposta.ok) {
        const erro = await resposta.json().catch(() => ({}));
        throw new Error(erro.mensagem ?? 'Não foi possível concluir a operação.');
    }
    return resposta.status === 204 ? undefined : resposta.json();
}

function mensagem(seletor, texto = '', erro = false) {
    const elemento = $(seletor);
    elemento.textContent = texto;
    elemento.hidden = !texto;
    elemento.classList.toggle('erro', erro);
}

function exibirLogin() {
    if ($('#cadastro').open) $('#cadastro').close();
    $('#form-login').reset();
    $('#painel').hidden = true;
    $('#sair').hidden = true;
    $('#acesso').hidden = false;
}

async function exibirPainel() {
    $('#acesso').hidden = true;
    $('#sair').hidden = false;
    $('#painel').hidden = false;
    await carregar('livros');
}

async function carregar(chave, aviso = '') {
    const pedido = ++carregamento;
    entidade = chave;
    const cadastro = cadastros[chave];
    $('#titulo').textContent = cadastro.titulo;
    $('#novo').textContent = cadastro.novo;
    $('#novo').disabled = true;
    $('#registros').replaceChildren();
    mensagem('#aviso');
    for (const aba of document.querySelectorAll('.aba')) {
        aba.setAttribute('aria-pressed', aba.dataset.entidade === chave);
    }
    const colunas = [...cadastro.colunas, 'Ações'].map((titulo) => {
        const th = document.createElement('th');
        th.scope = 'col';
        th.textContent = titulo;
        return th;
    });
    $('#colunas').replaceChildren(...colunas);

    try {
        const registros = await api('/' + chave);
        const listaLivros = chave === 'vendas' ? await api('/livros') : [];
        if (pedido !== carregamento) return;
        livros = listaLivros;
        for (const registro of registros) {
            adicionarLinha(cadastro, registro);
        }
        if (registros.length === 0) {
            const td = $('#registros').insertRow().insertCell();
            td.colSpan = colunas.length;
            td.textContent = 'Nenhum registro cadastrado.';
        }
        mensagem('#aviso', aviso);
        $('#novo').disabled = false;
    } catch (erro) {
        if (pedido === carregamento) mensagem('#aviso', erro.message, true);
    }
}

function adicionarLinha(cadastro, registro) {
    const linha = $('#registros').insertRow();
    for (const texto of cadastro.linha(registro)) {
        linha.insertCell().textContent = texto;
    }

    const acoes = linha.insertCell();
    acoes.className = 'acoes';
    const editar = document.createElement('button');
    editar.type = 'button';
    editar.className = 'secundario editar';
    editar.textContent = 'Editar';
    editar.addEventListener('click', () => abrirFormulario(registro));

    const excluir = document.createElement('button');
    excluir.type = 'button';
    excluir.className = 'secundario excluir';
    excluir.textContent = 'Excluir';
    excluir.addEventListener('click', () => excluirRegistro(registro));
    acoes.append(editar, excluir);
}

function abrirFormulario(registro) {
    if (entidade === 'vendas' && livros.length === 0) {
        mensagem('#aviso', 'Cadastre um livro antes de registrar uma venda.', true);
        return;
    }

    registroEmEdicao = registro;
    const cadastro = cadastros[entidade];
    $('#titulo-form').textContent = registro ? 'Editar ' + cadastro.singular : cadastro.novo;
    mensagem('#erro-cadastro');
    const campos = $('#campos');
    campos.replaceChildren();
    for (const { nome, rotulo, opcoes, ...atributos } of cadastro.campos) {
        const campo = document.createElement(opcoes ? 'select' : 'input');
        Object.assign(campo, atributos);
        campo.name = nome;
        campo.id = 'campo-' + nome;
        campo.required = !(registro && nome === 'senha');
        if (opcoes) {
            campo.add(new Option('Selecione', ''));
            for (const [valor, texto] of opcoes()) campo.add(new Option(texto, valor));
        }
        campo.value = registro?.[nome] ?? '';
        const label = document.createElement('label');
        label.htmlFor = campo.id;
        label.textContent = registro && nome === 'senha' ? 'Nova senha (opcional)' : rotulo;
        campos.append(label, campo);
    }
    $('#cadastro').showModal();
}

async function excluirRegistro(registro) {
    const chave = entidade;
    if (!window.confirm('Excluir este registro?')) return;
    try {
        await api('/' + chave + '/' + registro.id, { method: 'DELETE' });
        if (entidade === chave) await carregar(chave, 'Registro excluído.');
    } catch (erro) {
        if (entidade === chave) mensagem('#aviso', erro.message, true);
    }
}

$('#form-login').addEventListener('submit', async (evento) => {
    evento.preventDefault();
    const botao = evento.submitter;
    botao.disabled = true;
    mensagem('#erro-login');
    try {
        const dados = new URLSearchParams(new FormData(evento.currentTarget));
        await api('/login', { method: 'POST', dados });
        evento.target.reset();
        await exibirPainel();
    } catch (erro) {
        mensagem('#erro-login', erro.message, true);
    } finally {
        botao.disabled = false;
    }
});

$('#sair').addEventListener('click', async () => {
    try {
        await api('/logout', { method: 'POST' });
        exibirLogin();
    } catch (erro) {
        mensagem('#aviso', erro.message, true);
    }
});

for (const aba of document.querySelectorAll('.aba')) {
    aba.addEventListener('click', () => carregar(aba.dataset.entidade));
}
$('#novo').addEventListener('click', () => abrirFormulario());
$('#cancelar').addEventListener('click', () => $('#cadastro').close());

$('#form-cadastro').addEventListener('submit', async (evento) => {
    evento.preventDefault();
    const botao = evento.submitter;
    const chave = entidade;
    const id = registroEmEdicao?.id;
    const dados = Object.fromEntries(new FormData(evento.currentTarget));
    for (const campo of ['preco', 'quantidade', 'livroId']) {
        if (campo in dados) dados[campo] = Number(dados[campo]);
    }
    botao.disabled = true;
    mensagem('#erro-cadastro');
    try {
        const caminho = '/' + chave + (id ? '/' + id : '');
        await api(caminho, { method: id ? 'PUT' : 'POST', dados });
        $('#cadastro').close();
        await carregar(chave, id ? 'Registro atualizado.' : 'Registro cadastrado.');
    } catch (erro) {
        mensagem('#erro-cadastro', erro.message, true);
    } finally {
        botao.disabled = false;
    }
});

api('/sessao').then(exibirPainel).catch(exibirLogin);
