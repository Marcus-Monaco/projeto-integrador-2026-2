# 7. Tela com o nome completo dos integrantes

O aplicativo terá uma tela chamada **"Sobre o app"** (T17), que exibe o **nome completo de todos os integrantes do grupo**, além da versão, do nome do projeto e das principais tecnologias.

![Wireframe da tela Sobre o app e integrantes](../assets/wireframes/17-sobre-integrantes.png)

## 7.1 Onde a tela fica no aplicativo

- **Caminho:** barra inferior → **Mais** → **Sobre o app** (T15 → T17).
- Está disponível **sem login e sem internet**, pois não depende do banco de dados: os nomes ficam fixos no código do aplicativo.
- O requisito também aparece como **RF28** em [Características](04-caracteristicas.md).

## 7.2 Conteúdo da tela

| Bloco | Conteúdo |
|---|---|
| Identificação | Logo, nome **MeuBalcão** e versão do aplicativo |
| Projeto | Projeto Integrador VI-B, curso e instituição |
| Integrantes do grupo | **Nome completo** de cada integrante e sua função no projeto |
| Tecnologia | Banco de dados local (SQLite) e framework (Flutter e Dart) |

## 7.3 Integrantes

> ⚠️ **Preencher antes de entregar.** Os campos abaixo estão entre colchetes e devem ser substituídos pelos **nomes completos, sem abreviações**. Inclua ou remova linhas conforme o tamanho do grupo.

| Nome completo | Função no projeto |
|---|---|
| Henrique Bin Estramar                | Gerente de projeto e documentação |
| Lucas Cechett Hofmann                | Modelagem e banco de dados |
| Mauricio Andryell Porgeri dos Santos | Desenvolvimento do aplicativo (Flutter) |
| Marcus Vinicius Sena Pereira         | Design de interface, testes e entrega do APK |

## 7.4 Como será implementada

A lista de integrantes fica em uma constante, de modo que, para atualizar a tela, basta editar um único arquivo.

```dart
// lib/ui/screens/sobre_screen.dart
import 'package:flutter/material.dart';

class Integrante {
  final String nome;
  final String funcao;
  const Integrante(this.nome, this.funcao);
}

const List<Integrante> integrantes = [
  Integrante('Henrique Bin Estramar', 'Gerente de projeto e documentação'),
  Integrante('Lucas Cechett Hofmann', 'Modelagem e banco de dados'),
  Integrante('Mauricio Andryell Porgeri dos Santos', 'Desenvolvimento do aplicativo (Flutter)'),
  Integrante('Marcus Vinicius Sena Pereira', 'Design de interface, testes e entrega do APK'),
];

class SobreScreen extends StatelessWidget {
  const SobreScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final tema = Theme.of(context).textTheme;
    return Scaffold(
      appBar: AppBar(title: const Text('Sobre o app')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Text('MeuBalcão', style: tema.headlineMedium),
          const Text('Versão 1.0.0'),
          const SizedBox(height: 16),
          const Text('Projeto Integrador VI-B'),
          const SizedBox(height: 24),
          Text('Integrantes do grupo', style: tema.titleMedium),
          for (final i in integrantes)
            ListTile(
              leading: const CircleAvatar(child: Icon(Icons.person)),
              title: Text(i.nome),
              subtitle: Text(i.funcao),
            ),
        ],
      ),
    );
  }
}
```

A versão exibida será lida automaticamente do `pubspec.yaml` com o pacote `package_info_plus` (ver [item 9](09-tecnologias.md)), de modo que a tela acompanhe cada nova versão gerada do APK.

## 7.5 Teste automatizado da tela

Um teste de widget garante que **todos** os integrantes continuam aparecendo:

```dart
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:meubalcao/ui/screens/sobre_screen.dart';

void main() {
  testWidgets('Tela Sobre exibe o nome de todos os integrantes', (tester) async {
    await tester.pumpWidget(const MaterialApp(home: SobreScreen()));
    for (final i in integrantes) {
      expect(find.text(i.nome), findsOneWidget);
    }
  });
}
```

## 7.6 Critérios de aceite

- [ ] A tela é acessível pelo menu **Mais → Sobre o app**.
- [ ] Todos os integrantes aparecem com o **nome completo**.
- [ ] A tela abre normalmente com o aparelho **em modo avião**.
- [ ] Os nomes na tela são **idênticos** aos listados neste repositório.
- [ ] Uma captura de tela da tela real será anexada ao relatório da etapa de implementação.
