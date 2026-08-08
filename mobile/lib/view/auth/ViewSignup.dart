import 'package:flutter/material.dart';

class ViewSignup extends StatefulWidget {
  const ViewSignup({super.key});
  @override
  _ViewSignup createState() => _ViewSignup();
}

bool AgreeTerms = false;

class _ViewSignup extends State<ViewSignup> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(),
      body: SingleChildScrollView(
        child: Padding(
          padding: const EdgeInsets.all(8.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                "Junte-se ao movimento",
                style: TextStyle(fontSize: 32, fontWeight: FontWeight.bold),
              ),
              Text(
                "Crie seu centro de produtividade profissional hoje mesmo.",
                style: TextStyle(fontSize: 14),
              ),
              SizedBox(height: 50),
              Text("NOME COMPLETO", style: TextStyle(fontSize: 14)),
              SizedBox(height: 5),
              TextFormField(
                decoration: InputDecoration(
                  prefixIcon: Icon(Icons.person_2_outlined),
                  labelText: "Semêão",
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular((10)),
                  ),
                ),
              ),
              SizedBox(height: 10),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text("ENDEREÇO DE E-MAIL", style: TextStyle(fontSize: 14)),
                  SizedBox(height: 5),
                  TextFormField(
                    obscureText: true,
                    decoration: InputDecoration(
                      labelText: "semeao@exemplo.com",
                      prefixIcon: Icon(Icons.mail_outlined),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular((10)),
                      ),
                    ),
                  ),
                  SizedBox(height: 20),
                  Row(
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text("SENHA"),
                            const SizedBox(height: 8),
                            TextFormField(
                              obscureText: true,
                              decoration: InputDecoration(
                                hintText: "•••••••",
                                prefixIcon: const Icon(Icons.lock_outline),
                                border: OutlineInputBorder(
                                  borderRadius: BorderRadius.circular(10),
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),

                      const SizedBox(width: 20),

                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            const Text("CONFIRMAR SENHA"),
                            const SizedBox(height: 8),
                            TextFormField(
                              obscureText: true,
                              decoration: InputDecoration(
                                hintText: "•••••••",
                                prefixIcon: const Icon(
                                  Icons.check_circle_outline,
                                ),
                                border: OutlineInputBorder(
                                  borderRadius: BorderRadius.circular(10),
                                ),
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                  SizedBox(height: 20),
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Checkbox(
                        value: AgreeTerms,
                        onChanged: (bool? value) {
                          setState(() {
                            AgreeTerms = value ?? false;
                          });
                        },
                      ),
                      Expanded(
                        child: RichText(
                          text: TextSpan(
                            children: [
                              TextSpan(
                                text: "Eu concordo com os ",
                                style: TextStyle(
                                  color: Colors.black,
                                  fontSize: 16,
                                ),
                              ),
                              TextSpan(
                                text: "Termos e Condições",
                                style: TextStyle(
                                  color: Colors.blue.shade900,
                                  fontSize: 16,
                                ),
                              ),
                              TextSpan(
                                text: " e com a Política de Privacidade.",
                                style: TextStyle(
                                  color: Colors.black,
                                  fontSize: 16,
                                ),
                              ),
                            ],
                          ),
                        ),
                      ),
                    ],
                  ),
                  SizedBox(height: 10),
                  SizedBox(
                    height: 57,
                    width: double.infinity,
                    child: ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.blue.shade900,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(06),
                        ),
                      ),
                      onPressed: () {},
                      child: Text(
                        "Criar Conta",
                        style: TextStyle(color: Colors.white, fontSize: 20),
                      ),
                    ),
                  ),
                  SizedBox(height: 22),

                  Row(
                    children: [
                      Expanded(child: Divider()),
                      Padding(
                        padding: EdgeInsets.symmetric(horizontal: 12),
                        child: Text(
                          "OU REGISTRE-SE COM",
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                      Expanded(child: Divider()),
                    ],
                  ),
                  SizedBox(height: 15),
                  Row(
                    children: [
                      Expanded(
                        child: OutlinedButton.icon(
                          onPressed: () {},
                          icon: Icon(Icons.g_mobiledata),
                          label: Text("Google"),
                          style: OutlinedButton.styleFrom(
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(10),
                            ),
                          ),
                        ),
                      ),

                      SizedBox(height: 10, width: 12),
                      Expanded(
                        child: OutlinedButton.icon(
                          onPressed: () {},
                          icon: Icon(Icons.code),
                          label: Text("Github"),
                          style: OutlinedButton.styleFrom(
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(10),
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                  SizedBox(height: 15),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text("Já tem uma conta?"),
                      TextButton(
                        onPressed: () {},
                        child: Text(
                          "Entrar",
                          style: TextStyle(
                            color: Colors.blue.shade900,
                            fontWeight: FontWeight(700),
                          ),
                        ),
                      ),
                    ],
                  ),
                  SizedBox(height: 15),
                  Center(
                    child: Text(
                      "© 2026 Project A. Gestão profissional de horários.",
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
