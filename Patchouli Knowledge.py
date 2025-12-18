import sqlite3, os, pandas

def drop(bd='./DB.db'):
    if os.path.exists(bd):os.remove(bd)
    else: print(f"O arquivo {bd} não foi encontrado.")


def create(bd='./DB.db'):
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


def insert(excel, bd='./DB.db'):
    with pandas.ExcelFile(excel) as file: sheet = pandas.read_excel(file, sheet_name='Livros')
    sheet = sheet.fillna("")
    dic = {col.lower(): list(sheet[col]) for col in sheet.columns}
    conn = sqlite3.connect(bd)
    cursor = conn.cursor()
    for livros in map(list, zip(*dic.values())):
        livros = ['' if item == '\\' else item for item in livros]
        livro, ptbr, idioma, autores, categorias = livros
        autores,categorias = autores.split('&'),categorias.split('&') #lista de autores, categorias p/livro

        #Adiciona as categorias, substitui seu id na lista de categorias p/livro
        for i, c in enumerate(categorias):
            try:
                cursor.execute('INSERT INTO Categorias (categoria) VALUES (?)', (c.strip(),))
                categorias[i] = cursor.lastrowid
            except sqlite3.IntegrityError:  # Se a categoria já estiver no bd ele coleta seu id
                categorias[i] = cursor.execute("SELECT _id FROM Categorias WHERE categoria = ?", (c.strip(),)).fetchone()[0]

        # Adiciona os autores, substitui seu id na lista de autores p/livro
        for i, a in enumerate(autores):
            try:
                cursor.execute('INSERT INTO Autores (nome) VALUES (?)', (a.strip(),))
                autores[i] = cursor.lastrowid
            except sqlite3.IntegrityError:  # Se o autor já estiver no bd ele coleta seu id
                autores[i] = cursor.execute("SELECT _id FROM Autores WHERE nome = ?", (a.strip(),)).fetchone()[0]

        # Adiciona o livro
        try:
            #Esse str(livro) é para os casos em que o nome do livro é somente um número. (1984 de Orwell, por exemplo)
            cursor.execute('''INSERT INTO Livros (titulo_original, titulo_pt_br, idioma)
                            VALUES (?,?,?)''', (str(livro).strip(), str(ptbr).strip(), idioma.strip()))
            livro = cursor.lastrowid
        except Exception as e:print(f'Erro no try do Livro. {e}')

        #Faz os vínculos entre as tabelas
        for cat in categorias:
            cursor.execute('''INSERT INTO Livros_Categorias  VALUES (?,?)''', (livro, cat))
        for autor in autores:
            cursor.execute('''INSERT INTO Livros_Autores VALUES (?,?)''', (livro, autor))

        conn.commit()

    cursor.close()
    conn.close()

drop(), create()
insert(input('Diretório do excel? '))