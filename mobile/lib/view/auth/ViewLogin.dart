import 'package:flutter/material.dart';

class ViewLogin extends StatefulWidget {
  const ViewLogin({super.key});
  @override
  _ViewLogin createState() => _ViewLogin();
}

class _ViewLogin extends State<ViewLogin> {
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
                "Bem-vindo de volta",
                style: TextStyle(fontSize: 32, fontWeight: FontWeight.bold),
              ),
              Text(
                "Por favor, insira seus dados para entrar.",
                style: TextStyle(fontSize: 14),
              ),
              SizedBox(height: 50),
              Text("Endereço de E-mail", style: TextStyle(fontSize: 14)),
              SizedBox(height: 5),
              TextFormField(
                decoration: InputDecoration(
                  prefixIcon: Icon(Icons.mail_outline),
                  labelText: "nome@empresa.com",
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular((10)),
                  ),
                ),
              ),
              SizedBox(height: 10),
              Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text("Senha", style: TextStyle(fontSize: 14)),
                      TextButton(
                        onPressed: () {},
                        child: Text(
                          "Esqueceu a senha?",
                          style: TextStyle(color: Colors.blue),
                        ),
                      ),
                    ],
                  ),
                  TextFormField(
                    obscureText: true,
                    decoration: InputDecoration(
                      hintText: "*******",
                      prefixIcon: Icon(Icons.lock_outline),
                      border: OutlineInputBorder(
                        borderRadius: BorderRadius.circular((10)),
                      ),
                    ),
                  ),
                  SizedBox(height: 20),
                  SizedBox(
                    height: 57,
                    width: double.infinity,
                    child: ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.blue,
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(06),
                        ),
                      ),
                      onPressed: () {},
                      child: Text(
                        "Entrar",
                        style: TextStyle(color: Colors.white, fontSize: 20),
                      ),
                    ),
                  ),
                  SizedBox(height: 45),
                  Row(
                    children: [
                      Expanded(child: Divider()),
                      Padding(
                        padding: EdgeInsets.symmetric(horizontal: 12),
                        child: Text(
                          "OU ENTRAR COM",
                          style: TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                      Expanded(child: Divider()),
                    ],
                  ),
                  SizedBox(height: 30),
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
                  SizedBox(height: 25),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text("Não tem uma conta?"),
                      TextButton(onPressed: () {}, child: Text("Criar Conta")),
                    ],
                  ),
                  SizedBox(height: 15),
                  Text("© 2026 Project A. Gestão profissional de horários."),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
