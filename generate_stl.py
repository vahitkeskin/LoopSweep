import math

def add_triangle(f, v1, v2, v3):
    # Calculate normal
    u = [v2[0]-v1[0], v2[1]-v1[1], v2[2]-v1[2]]
    v = [v3[0]-v1[0], v3[1]-v1[1], v3[2]-v1[2]]
    nx = u[1]*v[2] - u[2]*v[1]
    ny = u[2]*v[0] - u[0]*v[2]
    nz = u[0]*v[1] - u[1]*v[0]
    l = math.sqrt(nx*nx + ny*ny + nz*nz)
    if l == 0: l = 1
    nx, ny, nz = nx/l, ny/l, nz/l

    f.write(f"  facet normal {nx} {ny} {nz}\n")
    f.write("    outer loop\n")
    f.write(f"      vertex {v1[0]} {v1[1]} {v1[2]}\n")
    f.write(f"      vertex {v2[0]} {v2[1]} {v2[2]}\n")
    f.write(f"      vertex {v3[0]} {v3[1]} {v3[2]}\n")
    f.write("    endloop\n")
    f.write("  endfacet\n")

def add_cylinder(f, r, h, z_offset, segments=32):
    for i in range(segments):
        a1 = (i / segments) * 2 * math.pi
        a2 = ((i + 1) / segments) * 2 * math.pi
        
        # Bottom center, Top center
        bc = (0, 0, z_offset)
        tc = (0, 0, z_offset + h)
        
        # Bottom edge points
        b1 = (r * math.cos(a1), r * math.sin(a1), z_offset)
        b2 = (r * math.cos(a2), r * math.sin(a2), z_offset)
        
        # Top edge points
        t1 = (r * math.cos(a1), r * math.sin(a1), z_offset + h)
        t2 = (r * math.cos(a2), r * math.sin(a2), z_offset + h)
        
        # Bottom face (normal points down)
        add_triangle(f, bc, b2, b1)
        # Top face (normal points up)
        add_triangle(f, tc, t1, t2)
        
        # Side faces (2 triangles per segment)
        add_triangle(f, b1, b2, t1)
        add_triangle(f, t1, b2, t2)

with open("robot_3d_model.stl", "w") as f:
    f.write("solid robot\n")
    # Base cylinder (radius 150, height 50)
    add_cylinder(f, 150, 50, 0, 64)
    # Lidar cylinder (radius 30, height 20)
    add_cylinder(f, 30, 20, 50, 32)
    f.write("endsolid robot\n")

print("robot_3d_model.stl generated!")
