import 'package:calendar/widget/WidgetBody.dart';
import 'package:calendar/widget/WidgetButton.dart';
import 'package:calendar/widget/WidgetInput.dart';
import 'package:flutter/material.dart';

class ViewForgotPassword extends StatefulWidget {
  const ViewForgotPassword({ Key? key }) : super(key: key);

  @override
  _ViewForgotPasswordState createState() => _ViewForgotPasswordState();
}

class _ViewForgotPasswordState extends State<ViewForgotPassword> {
  @override
  Widget build(BuildContext context) {
    return WidgetBody(
      children: [
        Text(
          "Esqueceu a senha?",
          style: TextStyle(fontSize: 32, fontWeight: FontWeight.bold),
        ),
        Text(
          "Por favor, insira seu email para redefinir sua senha.",
          style: TextStyle(fontSize: 14),
        ),
        SizedBox(height: 50),

        WidgetInput(label: "Email"),

        WidgetButton(text: "Redefinir senha"),
      ],
    );
  }
}