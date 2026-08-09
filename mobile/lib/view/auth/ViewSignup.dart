import 'package:calendar/widget/WidgetBody.dart';
import 'package:calendar/widget/WidgetButton.dart';
import 'package:calendar/widget/WidgetInput.dart';
import 'package:calendar/widget/WidgetOAuthButton.dart';
import 'package:flutter/material.dart';

class ViewSignup extends StatefulWidget {
  const ViewSignup({super.key});
  @override
  _ViewSignup createState() => _ViewSignup();
}

bool bool_terms = false;

class _ViewSignup extends State<ViewSignup> {
  @override
  Widget build(BuildContext context) {
    return WidgetBody(
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

        WidgetInput(
          label: "Digite seu nome"
        ),
        
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
        
            WidgetInput(
              label: "Digite seu email"
            ),
            
            Row(
              children: [
                Expanded(
                  child: WidgetInput(
                    label: "Digite sua senha",
                    password: true,
                    icon: Icons.lock_outline,
                  ),
                ),

                const SizedBox(width: 20),

                Expanded(
                  child: WidgetInput(
                    label: "Digite sua senha novamente",
                    password: true,
                    icon: Icons.lock_outline,
                  ),
                ),
              ],
            ),
            SizedBox(height: 20),
            GestureDetector(
              onTap: () {
                setState(() {
                  bool_terms = !bool_terms;
                });
              },
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Checkbox(
                    value: bool_terms,
                    onChanged: (bool? value) {
                      //setState(() {
                      //  bool_terms = value ?? false;
                      //});
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
            ),
            SizedBox(height: 10),
            
            WidgetButton(text: "Criar conta"),

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
                  child: WidgetOAuthButton(
                    text: "Google",
                    icon: Icons.g_mobiledata
                  ),
                ),

                SizedBox(height: 10, width: 12),
                Expanded(
                  child: WidgetOAuthButton(
                    text: "GitHub",
                    icon: Icons.code
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
      ]
    );
  }
}
