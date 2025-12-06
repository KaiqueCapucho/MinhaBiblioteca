import sqlite3, os

def drop(bd='./DB.db'):
    if os.path.exists(bd):os.remove(bd)
    else: print(f"O arquivo {bd} não foi encontrado.")
def create(bd=('./DB.db')):
    conn = sqlite3.connect(bd)
    cursor = conn.cursor()

    cursor.execute('''CREATE TABLE IF NOT EXISTS Livros (_id INTEGER PRIMARY KEY AUTOINCREMENT, 
    titulo_original STRING, titulo_pt_br STRING NOT NULL, idioma STRING NOT NULL, ano_pub INTEGER,
    editora STRING, descricao STRING, obtido BOOLEAN);''')

    cursor.execute(''' CREATE TABLE IF NOT EXISTS Autores (_id INTEGER PRIMARY KEY AUTOINCREMENT, 
    nome STRING COLLATE NOCASE UNIQUE, nascimento STRING, nacionalidade STRING, biografia STRING);''')

    cursor.execute(''' CREATE TABLE IF NOT EXISTS Categorias (_id INTEGER PRIMARY KEY AUTOINCREMENT, 
    categoria STRING UNIQUE NOT NULL);''')

    cursor.execute(''' CREATE TABLE IF NOT EXISTS Temas (_id INTEGER PRIMARY KEY AUTOINCREMENT,
     tema STRING UNIQUE NOT NULL);''')


    cursor.execute('''
    CREATE TABLE IF NOT EXISTS Livros_Temas (
        livro_id INTEGER, tema_id INTEGER, PRIMARY KEY (livro_id, tema_id),
        FOREIGN KEY (livro_id) REFERENCES Livros(id), FOREIGN KEY (tema_id) REFERENCES Temas(id));''')

    cursor.execute('''
    CREATE TABLE IF NOT EXISTS Livros_Autores (
        livro_id INTEGER, autor_id INTEGER, PRIMARY KEY (livro_id, autor_id),
        FOREIGN KEY (livro_id) REFERENCES Livros(id), FOREIGN KEY (autor_id) REFERENCES Autores(id));''')

    cursor.execute('''
    CREATE TABLE IF NOT EXISTS Livros_Categorias (
        livro_id INTEGER, categoria_id INTEGER, PRIMARY KEY (livro_id, categoria_id),
        FOREIGN KEY (livro_id) REFERENCES Livros(id),FOREIGN KEY (categoria_id) REFERENCES Categorias(id));''')
    conn.commit()
    cursor.close(),conn.close()


def insert(bd=('./DB.db')):
    conn = sqlite3.connect(bd)
    cursor = conn.cursor()
    with open('./livros.txt', encoding='utf-8') as books:
        category = ''
        for line in books.readlines():
            if line.startswith('---'):
                cursor.execute('INSERT INTO Categorias (categoria) VALUES (?)', (line[3:],))
                categoryID = cursor.lastrowid
            else:
                book, author, *tip = line.strip().split('\t')
                if tip: tip = tip[0].strip()
                else: tip = None
                try:
                    cursor.execute('INSERT INTO Autores (nome) VALUES (?)', (author.strip(),))
                    authorID = cursor.lastrowid
                except sqlite3.IntegrityError: pass
                try:
                    cursor.execute('''INSERT INTO Livros (titulo_pt_br, descricao, idioma)
                                        VALUES (?,?,?)''', (book.strip(),tip, 'português'))
                    bookID = cursor.lastrowid
                except: pass
                cursor.execute('''INSERT INTO Livros_Autores VALUES (?,?)''', (bookID,authorID))
                cursor.execute('''INSERT INTO Livros_Categorias VALUES (?,?)''', (bookID,categoryID))
    conn.commit()
    cursor.close(), conn.close()

#drop()
#create()
insert()