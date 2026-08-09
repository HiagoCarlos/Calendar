import 'package:flutter/material.dart';

class WidgetButton extends StatefulWidget {
    const WidgetButton({
      super.key,
      this.text=""
    });

    final String text;

    @override
    _WidgetButton createState() => _WidgetButton();
}

class _WidgetButton extends State<WidgetButton> {

    

    @override
    Widget build(BuildContext context) {
        return Column(
          children: [
            SizedBox(
              //height: 57,
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
                  widget.text,
                  style: TextStyle(color: Colors.white, fontSize: 20),
                ),
              ),
            ),
            SizedBox(height: 22),
          ]
        );
    }
}
