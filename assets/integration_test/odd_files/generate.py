with open('no_newlines2', 'w') as myfile:
    for _ in range(1000000000): # ~1 gb
        myfile.writelines("a")