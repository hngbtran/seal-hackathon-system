import {
    DndContext, closestCenter,
    KeyboardSensor, PointerSensor, useSensor, useSensors,
} from '@dnd-kit/core'
import {
    SortableContext, sortableKeyboardCoordinates,
    verticalListSortingStrategy, arrayMove,
} from '@dnd-kit/sortable'
import { Plus, SquaresFour } from '@phosphor-icons/react'
import SortableCard from '../../../../../components/shared/SortableCard'
import FormInput from '../../../../../components/shared/FormInput'
import FormTextarea from '../../../../../components/shared/FormTextarea'
import NoticeBox from '../../../../../components/shared/NoticeBox'
import { Info } from '@phosphor-icons/react'
import styles from './Step5Categories.module.css'

// ── Avatar: số thứ tự ──
function CategoryNumber({ index }) {
    return (
        <div className={styles.numberBadge}>
            {index + 1}
        </div>
    )
}

// ── Nội dung 1 category card ──
function CategoryCardContent({ category, onChange, errors }) {
    function update(field, val) {
        const value = val?.target ? val.target.value : val
        onChange({ ...category, [field]: value })
    }

    const minTeamError = errors?.[`category-${category.id}-minTeam`] || (() => {
        if (category.minTeam === '' || category.minTeam === undefined || category.minTeam === null) {
            return 'Vui lòng nhập số đội tối thiểu'
        }
        if (category.minTeam && category.teamLimit && Number(category.minTeam) > Number(category.teamLimit)) {
             return 'Số lượng tối thiểu không được lớn hơn số lượng tối đa'
        }
        return null
    })();

    return (
        <>
            {/* Hàng 1: icon + tên hạng mục */}
            <div className={styles.nameRow}>
                <div className={styles.categoryIcon}>
                    <SquaresFour size={32} weight="fill" color="var(--color-secondary-blue)" />
                </div>
                <div className={styles.nameField}>
                    <span className={styles.nameLabel}>
                        Tên hạng mục
                        <span className={styles.required}> *</span>
                    </span>
                    <FormInput
                        placeholder="Nhập tên hạng mục..."
                        value={String(category.name ?? '')}
                        onChange={val => update('name', val)}
                        status={errors?.[`category-${category.id}-name`] ? 'error' : 'default'}
                        message={errors?.[`category-${category.id}-name`]}
                    />
                </div>
            </div>

            {/* Hàng 2: mô tả + giới hạn số đội */}
            <div className={styles.bodyRow}>
                <div className={styles.fieldGroup}>
                    <label className={styles.fieldLabel}>Mô tả hạng mục</label>
                    <FormTextarea
                        className={styles.textArea}
                        rows={3}
                        placeholder="Mô tả chủ đề, vấn đề và định hướng giải pháp của hạng mục này..."
                        value={String(category.desc ?? '')}
                        onChange={e => update('desc', e.target.value)}
                    />
                </div>

                <div className={styles.fieldGroup}>
                    <label className={styles.fieldLabel}>
                        Số đội tối thiểu
                        <span className={styles.required}> *</span>
                    </label>
                    <FormInput
                        required
                        type="number"
                        min={1}
                        className={styles.baseInput}
                        placeholder="Số đội tối thiểu tham gia"
                        value={category.minTeam ?? ''}
                        onChange={e => {
                            const val = e.target.value
                            if (val !== '' && Number(val) < 1) return
                            update('minTeam', val === '' ? '' : Number(val))
                        }}
                        onKeyDown={e => {
                            if (['-', '+', 'e', 'E', '.', ','].includes(e.key)) {
                                e.preventDefault()
                            }
                        }}
                        status={minTeamError ? 'error' : 'default'}
                        message={minTeamError}
                    />
                </div>

                <div className={styles.fieldGroup}>
                    <label className={styles.fieldLabel}>
                        Giới hạn số đội
                        <span className={styles.required}> *</span>
                    </label>
                    <FormInput
                        required
                        type="number"
                        min={1}
                        className={styles.baseInput}
                        placeholder="Số đội tối đa tham gia hạng mục"
                        value={category.teamLimit ?? ''}
                        onChange={e => {
                            const val = e.target.value
                            if (val !== '' && Number(val) < 1) return
                            update('teamLimit', val === '' ? '' : Number(val))
                        }}
                        onKeyDown={e => {
                            if (['-', '+', 'e', 'E', '.', ','].includes(e.key)) {
                                e.preventDefault()
                            }
                        }}
                        status={(errors?.[`category-${category.id}-teamLimit`] || (!category.teamLimit ? 'error' : null)) ? 'error' : 'default'}
                        message={errors?.[`category-${category.id}-teamLimit`] || (!category.teamLimit ? 'Vui lòng nhập giới hạn số đội' : null)}
                    />
                </div>
            </div>
        </>
    )
}

// ── Main component ──
function Step5Categories({ formData, onFormChange, errors }) {
    const categories = formData.categories ?? [
        { id: 'cat-1', name: '', desc: '', minTeam: 5, teamLimit: '' }
    ]


    const sensors = useSensors(
        useSensor(PointerSensor),
        useSensor(KeyboardSensor, { coordinateGetter: sortableKeyboardCoordinates })
    )

    function handleDragEnd({ active, over }) {
        if (!over || active.id === over.id) return
        const oldIndex = categories.findIndex(c => c.id === active.id)
        const newIndex = categories.findIndex(c => c.id === over.id)
        onFormChange('categories', arrayMove(categories, oldIndex, newIndex))
    }

    function addCategory() {
        const maxId = categories.reduce((max, cat) => {
            const idNum = Number(cat.id);
            return !isNaN(idNum) && idNum > max ? idNum : max;
        }, 0);
        const nextId = maxId + 1;
        
        onFormChange('categories', [
            ...categories,
            { id: nextId, name: '', desc: '', minTeam: 5, teamLimit: '' },
        ])
    }

    function updateCategory(id, updated) {
        onFormChange('categories', categories.map(c => c.id === id ? { ...updated, id } : c))
    }

    function deleteCategory(id) {
        onFormChange('categories', categories.filter(c => c.id !== id))
    }

    return (
        <div className={styles.wrapper}>

            <div className={styles.headerRow}>
                <h1 className={styles.title}>Hạng mục</h1>
                <div className={styles.totalMinTeamDisplay}>
                    <span>Tổng số lượng tối thiểu các đội (toàn sự kiện): </span>
                    <strong>{categories.reduce((sum, cat) => sum + (Number(cat.minTeam) || 0), 0)} đội</strong>
                </div>
            </div>

            {/* <NoticeBox
                color="blue"
                icon={Info}
                message="Mentor và giám khảo đều có thể được phân công theo từng hạng mục, thiết lập ở Bước 7."
            /> */}

            <DndContext
                sensors={sensors}
                collisionDetection={closestCenter}
                onDragEnd={handleDragEnd}
            >
                <SortableContext
                    items={categories.map(c => c.id)}
                    strategy={verticalListSortingStrategy}
                >
                    <div className={styles.categoryList}>
                        {categories.map((cat, index) => (
                            <SortableCard
                                key={cat.id}
                                id={cat.id}
                                onDelete={() => deleteCategory(cat.id)}
                                showDelete={categories.length > 1}   
                                avatar={<CategoryNumber index={index} />}
                            >
                                <CategoryCardContent
                                    category={cat}
                                    onChange={updated => updateCategory(cat.id, updated)}
                                    errors={errors}
                                />
                            </SortableCard>
                        ))}
                    </div>
                </SortableContext>
            </DndContext>

            <button type="button" className={styles.addBtn} onClick={addCategory}>
                <Plus size={16} weight="bold" />
                Thêm hạng mục
            </button>

        </div>
    )
}

export default Step5Categories
