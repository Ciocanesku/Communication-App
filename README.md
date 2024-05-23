1. **Clasa `Client`**:
   - Această clasă reprezintă clientul care se conectează la server.
   - Utilizează un `Socket` pentru a stabili conexiunea la server.
   - Folosește obiecte `ObjectOutputStream` și `ObjectInputStream` pentru a trimite și a primi obiecte între client și server.
   - Are o buclă de citire a mesajelor de la utilizator și trimitere către server, și o buclă de citire a mesajelor primite de la server.

2. **Clasa `Server`**:
   - Această clasă reprezintă serverul care primește conexiuni de la clienți.
   - Utilizează un `ServerSocket` pentru a asculta conexiunile de la clienți pe un port specificat.
   - Odată ce primește o conexiune de la un client, creează obiecte `ObjectOutputStream` și `ObjectInputStream` pentru a comunica cu clientul.
   - Are o buclă care așteaptă și procesează mesajele primite de la client și trimite răspunsuri înapoi.

4. **Trimitere și Primire de Mesaje**:
   - Clientul poate trimite mesaje text către server.
   - Serverul primește mesajele text de la un client.

5. **Transfer de Fișiere**:
   - Clientul poate trimite fișiere către server, și poate primi fișiere de la server.
   - Clientul poate trimite fișiere către server, și poate primi fișiere de la server.
   - Pentru transferul de fisiere, mesajul are structura "FILE:path_of_file_to_send".
     
6. **Clasa `ClientData`**:
   - Această clasă este o simplă clasă de date care conține informațiile necesare pentru autentificarea clientului la server.
   - Obiectele de tip `ClientData` sunt serializate și trimise de la client către server.

7. **Autentificarea Clientului**:
   - În momentul conectării la server, clientul solicită utilizatorului să introducă un username și o parolă.
   - Aceste informații sunt folosite pentru a crea un obiect de tip `ClientData`.
   - Obiectul `ClientData` este apoi trimis către server folosind un flux de ieșire obiect (`ObjectOutputStream`).

8. **Recepționarea și Procesarea ClientData la Server**:
   - Serverul primește obiectul `ClientData` folosind un flux de intrare obiect (`ObjectInputStream`).
   - Obiectul `ClientData` conține informațiile de autentificare ale clientului (username și parolă).
   - Serverul verifică aceste informații în baza de date pentru a autentifica și autoriza clientul.
