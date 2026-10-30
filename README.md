# 📚 Sistema de Gestão de Biblioteca Universitária

Aplicação desktop em **Java** para gerenciar o acervo, os leitores e os empréstimos de uma biblioteca universitária. Desenvolvida como trabalho da disciplina de Programação Orientada a Objetos (Tema 5), com foco na aplicação prática dos pilares da POO: **encapsulamento, herança, polimorfismo, abstração e interfaces**.

A interação é feita por caixas de diálogo (`JOptionPane` / Swing), sem necessidade de banco de dados ou bibliotecas externas.

---

## ✨ Funcionalidades

| Opção | Funcionalidade |
|-------|----------------|
| 1 | **Cadastrar obra**: publicação impressa, publicação digital, livro físico ou e-book |
| 2 | **Cadastrar leitor**: aluno ou professor |
| 3 | **Realizar empréstimo** (com opção de reservar quando a obra está esgotada) |
| 4 | **Registrar devolução**, com cálculo automático de multa |
| 5 | **Consultar acervo**: por título, por ano e autor, ou ficha completa por ID |
| 6 | **Renovar empréstimo** |
| 7 | **Doação e reposição** de exemplares |
| 8 | **Histórico do leitor** |
| 9 | **Relatório de obras pendentes** |
| 0 | Sair |

### Regras de negócio

- **Prazo de empréstimo:** 7 dias, com até **2 renovações** (de 7 dias cada). A renovação é negada se o empréstimo já foi encerrado, se o limite foi atingido ou se há reserva pendente para a obra.
- **Limite de empréstimos simultâneos:** 3 para alunos e 5 para professores.
- **Fila de reservas:** o status da obra muda automaticamente entre `DISPONIVEL`, `EMPRESTADA` e `RESERVADA`.
- **Capacidade da estante:** 100 volumes físicos. Obras digitais não ocupam espaço na estante.
- **Multas por atraso** (cada tipo de obra calcula a sua):

| Tipo de obra | Cálculo da multa |
|--------------|------------------|
| Publicação impressa | R$ 2,00 por dia de atraso |
| Livro físico | R$ 2,00 por dia + R$ 5,00 de conservação do papel |
| Publicação digital | Taxa administrativa fixa de R$ 4,00 |
| E-book | Taxa fixa de R$ 2,50 |

### Recursos específicos por tipo de obra

- **Publicação impressa / Livro físico:** vistoria de desgaste da capa, agendamento de restauro e conferência do volume na estante (livro físico, com validação de ISBN de 10 ou 13 dígitos).
- **Publicação digital:** controle de licenças ativas e de downloads temporários (limite de 5).
- **E-book:** limite de leitores simultâneos, reprodução de áudio por capítulo (máx. 2 simultâneas), retomada de leitura, ajuste de acessibilidade (fonte e sintetizador de voz) e leitura offline.

---

## 🧱 Arquitetura

O projeto segue o padrão **MVC**:

```
src/
├── Main.java                         # Ponto de entrada
├── model/                            # Entidades e regras do domínio
│   ├── ObraAcervo.java               #   Classe abstrata base
│   ├── PublicacaoImpressa.java       #   └─ LivroFisico.java
│   ├── PublicacaoDigital.java        #   └─ Ebook.java
│   ├── Leitor.java
│   └── Emprestimo.java
├── view/
│   └── BibliotecaView.java           # Menus e diálogos (Swing)
├── controller/
│   └── BibliotecaController.java     # Orquestra cadastros, empréstimos e relatórios
├── interfaces/
│   └── Audivel.java                  # Contrato para conteúdo em áudio
├── enums/
│   ├── StatusObra.java               # DISPONIVEL, EMPRESTADA, RESERVADA
│   └── EstadoConservacao.java        # NOVO, BOM, DESGASTADO, DANIFICADO
└── exceptions/                       # Exceções personalizadas
    ├── ObraEsgotadaException.java
    ├── LimiteEmprestimoExcedidoException.java
    └── RenovacaoNegadaException.java
```

### Hierarquia de classes

```
ObraAcervo (abstract)
├── PublicacaoImpressa
│   └── LivroFisico
└── PublicacaoDigital
    └── Ebook (final, implements Audivel)
```

### Conceitos de POO aplicados

- **Abstração:** `ObraAcervo` define o contrato com o método abstrato `calcularMulta(int)`.
- **Herança:** `LivroFisico` e `Ebook` especializam as publicações impressa e digital.
- **Polimorfismo:** `Emprestimo.devolver()` chama `obra.calcularMulta()` sem saber o tipo concreto da obra.
- **Encapsulamento:** atributos privados com validação nos construtores e setters.
- **Interface:** `Audivel`, com constante e método `default`.
- **Template Method:** `montarFicha()` é final e usa o gancho `detalhesEspecificos()`, que as subclasses sobrescrevem.
- **Membros estáticos e `final`:** contadores (`totalVolumes`, códigos de empréstimo) e o método final `processarDoacao()`.
- **Sobrecarga:** `buscarAcervo(String)` e `buscarAcervo(int, String)`.
- **Exceções personalizadas** (checked) com informações úteis para o usuário.

---

## 🚀 Como executar

### Pré-requisitos

- **JDK 11** ou superior (o projeto usa `String.isBlank()`)

### Opção 1: pelo JAR pronto

```bash
java -jar projetofase2.jar
```

### Opção 2: compilando pelo terminal

```bash
cd projetofase2/src
javac -d ../out Main.java model/*.java view/*.java controller/*.java interfaces/*.java enums/*.java exceptions/*.java
java -cp ../out Main
```

### Opção 3: pela IDE

Abra a pasta `projetofase2` no **IntelliJ IDEA** (o projeto já inclui o arquivo `.iml`) e execute a classe `Main`.

> ⚠️ Por usar interface gráfica (Swing), é necessário um ambiente com display; não funciona em terminais sem suporte gráfico.

---

## 🛠️ Tecnologias

- Java
- Swing (`JOptionPane`)
- Java Time API (`LocalDate`)

---

## 👥 Autoras

Projeto desenvolvido por **Mariana Araujo Azevedo** ([@mariana-developer](https://github.com/mariana-developer)) e **Rute Dantas**, estudantes de Sistemas de Informação na UNIFACISA.

---

## 📄 Licença

Projeto acadêmico, sem fins comerciais.
